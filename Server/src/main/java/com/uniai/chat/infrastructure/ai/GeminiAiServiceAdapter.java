package com.uniai.chat.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.dto.ai.AiOperation;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.ConversationMemoryPromptFormatter;
import com.uniai.chat.application.port.out.AiProviderStatusPort;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.provider.AiProviderFailureCategory;
import com.uniai.chat.infrastructure.metrics.ChatAiMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gemini-backed implementation of {@link AiServicePort}.
 * All Gemini-specific transport and response parsing stays inside infrastructure.
 */
public class GeminiAiServiceAdapter implements AiServicePort {

    private static final Logger logger = LogManager.getLogger(GeminiAiServiceAdapter.class);
    private static final String FALLBACK_MESSAGE = "AI service error : this message is from GeminiAiServiceAdapter. Please try again later.";

    private final GeminiAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final AiProviderStatusPort statusPort;
    private final MeterRegistry meterRegistry;

    public GeminiAiServiceAdapter(GeminiAiProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, buildRestTemplate(), null, null);
    }

    public GeminiAiServiceAdapter(GeminiAiProperties properties, ObjectMapper objectMapper, AiProviderStatusPort statusPort) {
        this(properties, objectMapper, statusPort, null);
    }

    public GeminiAiServiceAdapter(GeminiAiProperties properties, ObjectMapper objectMapper, AiProviderStatusPort statusPort, MeterRegistry meterRegistry) {
        this(properties, objectMapper, buildRestTemplate(), statusPort, meterRegistry);
    }

    public GeminiAiServiceAdapter(GeminiAiProperties properties, ObjectMapper objectMapper, RestTemplate restTemplate, AiProviderStatusPort statusPort) {
        this(properties, objectMapper, restTemplate, statusPort, null);
    }

    public GeminiAiServiceAdapter(GeminiAiProperties properties, ObjectMapper objectMapper, RestTemplate restTemplate, AiProviderStatusPort statusPort, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.statusPort = statusPort;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public AiResponse generateResponse(AiRequest request) {
        long requestStartNanos = System.nanoTime();
        String userMessage = request != null ? request.getUserMessage() : null;
        String operation = resolveOperation(request);
        recordRequest(request, resolveModel(), operation);
        logger.debug("[PROVIDER] Request started provider=gemini model={} baseUrl={} messageLength={} historyCount={} contextCount={}",
                resolveModel(),
                normalizeBaseUrl(properties.getBaseUrl()),
                StringUtils.hasText(userMessage) ? userMessage.length() : 0,
                request != null && request.getConversationHistory() != null ? request.getConversationHistory().size() : 0,
                request != null && request.getContext() != null ? request.getContext().size() : 0);
        if (!StringUtils.hasText(userMessage)) {
            logger.warn("[PROVIDER] Empty request received provider=gemini model={}", resolveModel());
            return failureResponse("gemini", resolveModel(), "Please enter a message.", AiProviderFailureCategory.UNKNOWN, false, requestStartNanos, false, operation);
        }

        if (!StringUtils.hasText(properties.getApiKey())) {
            logger.warn("Gemini provider selected but ai.gemini.api-key is missing or blank");
            return failureResponse("gemini", resolveModel(), "Gemini is not configured. Please set ai.gemini.api-key.",
                    AiProviderFailureCategory.MISCONFIGURED, false, requestStartNanos, true, operation);
        }

        String model = resolveModel();
        String baseUrl = normalizeBaseUrl(properties.getBaseUrl());
        String url = baseUrl + "/models/" + model + ":generateContent";

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    buildRequest(request, userMessage),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                logger.warn("[PROVIDER] Empty or non-success Gemini response status={} durationMs={}",
                        response.getStatusCode().value(),
                        elapsedMillis(requestStartNanos));
                AiProviderFailureCategory failureCategory = !StringUtils.hasText(response.getBody())
                        ? AiProviderFailureClassifier.classifyEmptyResponse()
                        : AiProviderFailureClassifier.classifyHttpStatus(response.getStatusCode().value());
                return failureResponse("gemini", model, FALLBACK_MESSAGE, failureCategory, failureCategory.isRetryable(), requestStartNanos, true, operation);
            }

            AiResponse aiResponse = toResponse(response.getBody(), model, requestStartNanos, operation);
            if (Boolean.TRUE.equals(aiResponse.getFallback())) {
                logger.warn("[PROVIDER] Gemini fallback generated model={} durationMs={} responseLength={}",
                        model,
                        elapsedMillis(requestStartNanos),
                        response.getBody().length());
            } else {
                logger.debug("[PROVIDER] Request completed provider=gemini model={} finishReason={} responseLength={} durationMs={}",
                        model,
                        aiResponse.getFinishReason(),
                        aiResponse.getContent() != null ? aiResponse.getContent().length() : 0,
                        elapsedMillis(requestStartNanos));
                recordSuccess("gemini", aiResponse.getModel(), requestStartNanos);
            }
            return aiResponse;
        } catch (RestClientResponseException ex) {
            logger.error("[PROVIDER] Gemini HTTP failure status={} durationMs={}",
                    ex.getStatusCode().value(),
                    elapsedMillis(requestStartNanos));
            AiProviderFailureCategory failureCategory = AiProviderFailureClassifier.classifyHttpStatus(ex.getStatusCode().value());
            return failureResponse("gemini", model, FALLBACK_MESSAGE, failureCategory, failureCategory.isRetryable(), requestStartNanos, true, operation);
        } catch (ResourceAccessException ex) {
            logger.warn("[PROVIDER] Gemini request could not be completed durationMs={} reason={}",
                    elapsedMillis(requestStartNanos),
                    ex.getMessage());
            AiProviderFailureCategory failureCategory = AiProviderFailureClassifier.classifyThrowable(ex);
            return failureResponse("gemini", model, FALLBACK_MESSAGE, failureCategory, failureCategory.isRetryable(), requestStartNanos, true, operation);
        } catch (IllegalArgumentException ex) {
            logger.warn("[PROVIDER] Gemini configuration error durationMs={} reason={}",
                    elapsedMillis(requestStartNanos),
                    ex.getMessage());
            return failureResponse("gemini", model, FALLBACK_MESSAGE, AiProviderFailureCategory.MISCONFIGURED, false, requestStartNanos, true, operation);
        } catch (Exception ex) {
            logger.error("[PROVIDER] Gemini parsing or unexpected failure durationMs={} reason={}",
                    elapsedMillis(requestStartNanos),
                    ex.getMessage(), ex);
            AiProviderFailureCategory failureCategory = AiProviderFailureClassifier.classifyThrowable(ex);
            if (failureCategory == AiProviderFailureCategory.UNKNOWN) {
                failureCategory = AiProviderFailureClassifier.classifyParseFailure();
            }
            return failureResponse("gemini", model, FALLBACK_MESSAGE, failureCategory, failureCategory.isRetryable(), requestStartNanos, true, operation);
        }
    }

    private HttpEntity<Map<String, Object>> buildRequest(AiRequest request, String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("x-goog-api-key", properties.getApiKey());

        Map<String, Object> body = new LinkedHashMap<>();

        String systemPrompt = composeSystemPrompt(request);

        if (StringUtils.hasText(systemPrompt)) {
            body.put("system_instruction", Map.of(
                    "parts", List.of(
                            Map.of("text", systemPrompt)
                    )
            ));
        }

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        if (request != null && request.getTemperature() != null) {
            generationConfig.put("temperature", request.getTemperature());
        }
        if (request != null && request.getMaxTokens() != null) {
            generationConfig.put("max_output_tokens", request.getMaxTokens());
        }
        if (!generationConfig.isEmpty()) {
            body.put("generation_config", generationConfig);
        }

        body.put("contents", buildContents(request, userMessage));

        return new HttpEntity<>(body, headers);
    }

    private List<Map<String, Object>> buildContents(AiRequest request, String userMessage) {
        List<Map<String, Object>> contents = new ArrayList<>();

        if (request != null && request.getConversationHistory() != null) {
            for (AiConversationMessage historyMessage : request.getConversationHistory()) {
                addContent(contents, historyMessage != null ? historyMessage.getRole() : null,
                        historyMessage != null ? historyMessage.getContent() : null);
            }
        }

        if (request != null && request.getContext() != null && !request.getContext().isEmpty()) {
            addContent(contents, "user", "Context:\n" + String.join("\n", request.getContext()));
        }

        addContent(contents, "user", userMessage);
        return contents;
    }

    private void addContent(List<Map<String, Object>> contents, String role, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }

        String effectiveRole = normalizeRole(role);
        if (effectiveRole == null) {
            return;
        }
        Map<String, Object> part = new LinkedHashMap<>();
        part.put("text", content);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", effectiveRole);
        message.put("parts", List.of(part));
        contents.add(message);
    }

    private String normalizeRole(String role) {
        if (!StringUtils.hasText(role)) {
            return "user";
        }

        String normalized = role.trim().toLowerCase();
        if ("assistant".equals(normalized)) {
            return "model";
        }
        if ("user".equals(normalized) || "model".equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private AiResponse toResponse(String responseBody, String model, long requestStartNanos, String operation) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return failureResponse("gemini", model, FALLBACK_MESSAGE, AiProviderFailureClassifier.classifyParseFailure(), true, requestStartNanos, true, operation);
        }

        JsonNode firstCandidate = candidates.get(0);
        String finishReason = firstCandidate.path("finishReason").isMissingNode()
                ? null
                : firstCandidate.path("finishReason").asText(null);

        StringBuilder output = new StringBuilder();
        JsonNode parts = firstCandidate.path("content").path("parts");
        if (parts.isArray()) {
            for (JsonNode part : parts) {
                JsonNode textNode = part.get("text");
                if (textNode != null && textNode.isTextual() && StringUtils.hasText(textNode.asText())) {
                    if (output.length() > 0) {
                        output.append('\n');
                    }
                    output.append(textNode.asText().trim());
                }
            }
        }

        String text = output.toString().trim();
        if (!StringUtils.hasText(text)) {
            return failureResponse("gemini", model, FALLBACK_MESSAGE, AiProviderFailureClassifier.classifyEmptyResponse(), true, requestStartNanos, true, operation);
        }

        return AiResponse.builder()
                .content(text)
                .provider("gemini")
                .model(model)
                .finishReason(finishReason)
                .fallback(false)
                .failureCategory(AiProviderFailureCategory.NONE)
                .retryable(false)
                .build();
    }

    private AiResponse failureResponse(String provider, String model, String message, AiProviderFailureCategory failureCategory, boolean retryable, long requestStartNanos, boolean updateStatus, String operation) {
        if (updateStatus) {
            recordFailure(provider, model, failureCategory, requestStartNanos);
            ChatAiMetrics.incrementCounter(
                    meterRegistry,
                    ChatAiMetrics.PROVIDER_FAILURES,
                    "AI provider failures",
                    "provider",
                    provider,
                    "model",
                    ChatAiMetrics.normalizeTagValue(model),
                    "operation",
                    ChatAiMetrics.normalizeTagValue(operation),
                    "failure_category",
                    ChatAiMetrics.normalizeEnumName(failureCategory)
            );
            ChatAiMetrics.incrementCounter(
                    meterRegistry,
                    ChatAiMetrics.FALLBACKS,
                    "AI fallback usage",
                    "operation",
                    ChatAiMetrics.normalizeTagValue(operation),
                    "reason",
                    "provider_fallback"
            );
        }
        return AiResponse.builder()
                .content(message)
                .provider(provider)
                .model(model)
                .fallback(true)
                .failureCategory(failureCategory)
                .retryable(retryable)
                .build();
    }

    private String composeSystemPrompt(AiRequest request) {
        String systemPrompt = request != null ? request.getSystemPrompt() : null;
        ConversationMemory conversationMemory = request != null ? request.getConversationMemory() : null;
        if (conversationMemory == null || conversationMemory.isEmpty()) {
            return systemPrompt;
        }
        String memoryText = ConversationMemoryPromptFormatter.render(conversationMemory);
        if (!StringUtils.hasText(memoryText)) {
            return systemPrompt;
        }
        if (!StringUtils.hasText(systemPrompt)) {
            return "Trusted conversation memory:\n" + memoryText;
        }
        return systemPrompt + "\n\nTrusted conversation memory:\n" + memoryText;
    }

    private String resolveModel() {
        return StringUtils.hasText(properties.getModel())
                ? properties.getModel().trim()
                : "gemini-2.5-flash";
    }

    private String normalizeBaseUrl(String baseUrl) {
        String effectiveBaseUrl = StringUtils.hasText(baseUrl)
                ? baseUrl.trim()
                : "https://generativelanguage.googleapis.com/v1beta";
        return effectiveBaseUrl.endsWith("/")
                ? effectiveBaseUrl.substring(0, effectiveBaseUrl.length() - 1)
                : effectiveBaseUrl;
    }

    private static RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = (int) Duration.ofSeconds(20).toMillis();
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);
        return new RestTemplate(factory);
    }

    private void recordSuccess(String provider, String model, long startNanos) {
        if (statusPort != null) {
            statusPort.recordSuccess(provider, model, elapsedMillis(startNanos));
        }
    }

    private void recordFailure(String provider, String model, AiProviderFailureCategory failureCategory, long startNanos) {
        if (statusPort != null) {
            statusPort.recordFailure(provider, model, failureCategory, elapsedMillis(startNanos));
        }
    }

    private void recordRequest(AiRequest request, String model, String operation) {
        ChatAiMetrics.incrementCounter(
                meterRegistry,
                ChatAiMetrics.PROVIDER_REQUESTS,
                "AI provider requests",
                "provider",
                "gemini",
                "model",
                ChatAiMetrics.normalizeTagValue(model),
                "operation",
                ChatAiMetrics.normalizeTagValue(operation),
                "outcome",
                "attempt"
        );
    }

    private String resolveOperation(AiRequest request) {
        AiOperation operation = request != null ? request.getOperation() : AiOperation.UNKNOWN;
        return ChatAiMetrics.normalizeEnumName(operation);
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
