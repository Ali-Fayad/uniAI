package com.uniai.chat.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.port.out.AiServicePort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * Ollama-backed implementation of {@link AiServicePort}.
 * Uses the local Ollama chat API and remains optional at runtime.
 */
public class OllamaAiServiceAdapter implements AiServicePort {

    private static final Logger logger = LogManager.getLogger(OllamaAiServiceAdapter.class);
    private static final String FALLBACK_MESSAGE = "AI service is temporarily unavailable. Please try again later.";

    private final OllamaAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public OllamaAiServiceAdapter(OllamaAiProperties properties) {
        this(properties, new ObjectMapper(), buildRestTemplate(properties));
    }

    OllamaAiServiceAdapter(OllamaAiProperties properties, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public AiResponse generateResponse(AiRequest request) {
        long requestStartNanos = System.nanoTime();
        String userMessage = request != null ? request.getUserMessage() : null;
        String model = resolveModel();
        String baseUrl = normalizeBaseUrl(properties.getBaseUrl());
        int messageCount = countMessages(request, userMessage);

        logger.debug("[PROVIDER] Request started provider=ollama model={} baseUrl={} messageLength={} messageCount={} historyCount={} contextCount={} contextPresent={}",
                model,
                baseUrl,
                StringUtils.hasText(userMessage) ? userMessage.length() : 0,
                messageCount,
                request != null && request.getConversationHistory() != null ? request.getConversationHistory().size() : 0,
                request != null && request.getContext() != null ? request.getContext().size() : 0,
                request != null && request.getContext() != null && !request.getContext().isEmpty());

        if (!StringUtils.hasText(userMessage)) {
            logger.warn("[PROVIDER] Empty request received provider=ollama model={}", model);
            return fallbackResponse("ollama", model, "Please enter a message.");
        }

        String url = baseUrl + "/api/chat";
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    buildRequest(request, userMessage, model),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                logger.warn("[PROVIDER] Empty or non-success Ollama response status={} durationMs={}",
                        response.getStatusCode().value(),
                        elapsedMillis(requestStartNanos));
                return fallbackResponse("ollama", model, FALLBACK_MESSAGE);
            }

            AiResponse aiResponse = toResponse(response.getBody(), model);
            if (Boolean.TRUE.equals(aiResponse.getFallback())) {
                logger.warn("[PROVIDER] Ollama fallback generated model={} durationMs={} responseLength={}",
                        aiResponse.getModel(),
                        elapsedMillis(requestStartNanos),
                        response.getBody().length());
            } else {
                logger.debug("[PROVIDER] Request completed provider=ollama model={} finishReason={} responseLength={} durationMs={} promptEvalCount={} evalCount={}",
                        aiResponse.getModel(),
                        aiResponse.getFinishReason(),
                        aiResponse.getContent() != null ? aiResponse.getContent().length() : 0,
                        elapsedMillis(requestStartNanos),
                        extractLong(response.getBody(), "prompt_eval_count"),
                        extractLong(response.getBody(), "eval_count"));
            }
            return aiResponse;
        } catch (RestClientResponseException ex) {
            logger.error("[PROVIDER] Ollama HTTP failure status={} durationMs={} baseUrl={} body={} category=OLLAMA_UNAVAILABLE",
                    ex.getStatusCode().value(),
                    elapsedMillis(requestStartNanos),
                    baseUrl,
                    safeBody(ex.getResponseBodyAsString()), ex);
            return fallbackResponse("ollama", model, FALLBACK_MESSAGE);
        } catch (ResourceAccessException ex) {
            logger.warn("[PROVIDER] Ollama connection failed provider=ollama model={} baseUrl={} durationMs={} category=OLLAMA_UNAVAILABLE reason={}",
                    model,
                    baseUrl,
                    elapsedMillis(requestStartNanos),
                    ex.getMessage());
            return fallbackResponse("ollama", model, FALLBACK_MESSAGE);
        } catch (Exception ex) {
            logger.error("[PROVIDER] Ollama parsing or unexpected failure provider=ollama model={} baseUrl={} durationMs={} reason={}",
                    model,
                    baseUrl,
                    elapsedMillis(requestStartNanos),
                    ex.getMessage(), ex);
            return fallbackResponse("ollama", model, FALLBACK_MESSAGE);
        }
    }

    private HttpEntity<Map<String, Object>> buildRequest(AiRequest request, String userMessage, String model) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("stream", Boolean.FALSE);
        body.put("messages", buildMessages(request, userMessage));

        Map<String, Object> options = new LinkedHashMap<>();
        if (request != null && request.getTemperature() != null) {
            options.put("temperature", request.getTemperature());
        }
        if (request != null && request.getMaxTokens() != null) {
            options.put("num_predict", request.getMaxTokens());
        }
        if (!options.isEmpty()) {
            body.put("options", options);
        }

        return new HttpEntity<>(body, headers);
    }

    private List<Map<String, Object>> buildMessages(AiRequest request, String userMessage) {
        List<Map<String, Object>> messages = new ArrayList<>();

        if (request != null && StringUtils.hasText(request.getSystemPrompt())) {
            addMessage(messages, "system", request.getSystemPrompt());
        }

        if (request != null && request.getConversationHistory() != null) {
            for (AiConversationMessage historyMessage : request.getConversationHistory()) {
                addMessage(messages, normalizeRole(historyMessage != null ? historyMessage.getRole() : null),
                        historyMessage != null ? historyMessage.getContent() : null);
            }
        }

        if (request != null && request.getContext() != null && !request.getContext().isEmpty()) {
            addMessage(messages, "system", "Retrieved official graduate context:\n" + String.join("\n", request.getContext()));
        }

        addMessage(messages, "user", userMessage);
        return messages;
    }

    private void addMessage(List<Map<String, Object>> messages, String role, String content) {
        if (!StringUtils.hasText(role) || !StringUtils.hasText(content)) {
            return;
        }

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        messages.add(message);
    }

    private String normalizeRole(String role) {
        if (!StringUtils.hasText(role)) {
            return "user";
        }
        String normalized = role.trim().toLowerCase();
        if ("assistant".equals(normalized) || "model".equals(normalized)) {
            return "assistant";
        }
        if ("system".equals(normalized) || "user".equals(normalized)) {
            return normalized;
        }
        return "user";
    }

    private AiResponse toResponse(String responseBody, String configuredModel) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode messageNode = root.path("message");
        String content = messageNode.path("content").asText(null);
        String returnedModel = root.path("model").asText(null);
        String finishReason = root.path("done_reason").isMissingNode()
                ? null
                : root.path("done_reason").asText(null);

        if (!StringUtils.hasText(content)) {
            return fallbackResponse("ollama", configuredModel, FALLBACK_MESSAGE);
        }

        return AiResponse.builder()
                .content(content.trim())
                .provider("ollama")
                .model(StringUtils.hasText(returnedModel) ? returnedModel : configuredModel)
                .finishReason(finishReason)
                .fallback(false)
                .build();
    }

    private AiResponse fallbackResponse(String provider, String model, String message) {
        return AiResponse.builder()
                .content(message)
                .provider(provider)
                .model(model)
                .fallback(true)
                .build();
    }

    private String resolveModel() {
        return StringUtils.hasText(properties.getModel())
                ? properties.getModel().trim()
                : "gemma3:4b";
    }

    private String normalizeBaseUrl(String baseUrl) {
        String effectiveBaseUrl = StringUtils.hasText(baseUrl)
                ? baseUrl.trim()
                : "http://localhost:11434";
        return effectiveBaseUrl.endsWith("/")
                ? effectiveBaseUrl.substring(0, effectiveBaseUrl.length() - 1)
                : effectiveBaseUrl;
    }

    private static RestTemplate buildRestTemplate(OllamaAiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutSeconds = properties != null && properties.getTimeoutSeconds() != null
                ? properties.getTimeoutSeconds()
                : 120;
        int timeoutMillis = (int) Duration.ofSeconds(timeoutSeconds).toMillis();
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);
        return new RestTemplate(factory);
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private int countMessages(AiRequest request, String userMessage) {
        int count = 0;
        if (request != null && StringUtils.hasText(request.getSystemPrompt())) {
            count++;
        }
        if (request != null && request.getConversationHistory() != null) {
            count += (int) request.getConversationHistory().stream()
                    .filter(historyMessage -> historyMessage != null && StringUtils.hasText(historyMessage.getContent()))
                    .count();
        }
        if (request != null && request.getContext() != null && !request.getContext().isEmpty()) {
            count++;
        }
        if (StringUtils.hasText(userMessage)) {
            count++;
        }
        return count;
    }

    private Long extractLong(String responseBody, String fieldName) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode value = root.path(fieldName);
            return value.isNumber() ? value.longValue() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String safeBody(String body) {
        if (!StringUtils.hasText(body)) {
            return "<empty>";
        }
        return body.length() > 500 ? body.substring(0, 500) + "..." : body;
    }
}
