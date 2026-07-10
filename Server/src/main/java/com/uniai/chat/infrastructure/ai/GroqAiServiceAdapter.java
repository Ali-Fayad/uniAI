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
 * Groq-backed implementation of {@link AiServicePort}.
 * Uses the OpenAI-compatible Chat Completions API.
 */
public class GroqAiServiceAdapter implements AiServicePort {

    private static final Logger logger = LogManager.getLogger(GroqAiServiceAdapter.class);
    private static final String FALLBACK_MESSAGE = "AI service is temporarily unavailable. Please try again later.";

    private final GroqAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public GroqAiServiceAdapter(GroqAiProperties properties) {
        this(properties, new ObjectMapper(), buildRestTemplate());
    }

    GroqAiServiceAdapter(GroqAiProperties properties, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public AiResponse generateResponse(AiRequest request) {
        long requestStartNanos = System.nanoTime();
        String userMessage = request != null ? request.getUserMessage() : null;
        logger.debug("[PROVIDER] Request started provider=groq model={} baseUrl={} messageLength={} historyCount={} contextCount={}",
                resolveModel(),
                normalizeBaseUrl(properties.getBaseUrl()),
                StringUtils.hasText(userMessage) ? userMessage.length() : 0,
                request != null && request.getConversationHistory() != null ? request.getConversationHistory().size() : 0,
                request != null && request.getContext() != null ? request.getContext().size() : 0);
        if (!StringUtils.hasText(userMessage)) {
            logger.warn("[PROVIDER] Empty request received provider=groq model={}", resolveModel());
            return fallbackResponse("groq", resolveModel(), "Please enter a message.");
        }

        if (!StringUtils.hasText(properties.getApiKey())) {
            logger.warn("Groq provider selected but ai.groq.api-key is missing or blank");
            return fallbackResponse("groq", resolveModel(), "Groq is not configured. Please set ai.groq.api-key.");
        }

        String model = resolveModel();
        String url = normalizeBaseUrl(properties.getBaseUrl()) + "/chat/completions";

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    buildRequest(request, userMessage, model),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                logger.warn("[PROVIDER] Empty or non-success Groq response status={} durationMs={}",
                        response.getStatusCode().value(),
                        elapsedMillis(requestStartNanos));
                return fallbackResponse("groq", model, FALLBACK_MESSAGE);
            }

            AiResponse aiResponse = toResponse(response.getBody(), model);
            if (Boolean.TRUE.equals(aiResponse.getFallback())) {
                logger.warn("[PROVIDER] Groq fallback generated model={} durationMs={} responseLength={}",
                        model,
                        elapsedMillis(requestStartNanos),
                        response.getBody().length());
            } else {
                logger.debug("[PROVIDER] Request completed provider=groq model={} finishReason={} responseLength={} durationMs={}",
                        model,
                        aiResponse.getFinishReason(),
                        aiResponse.getContent() != null ? aiResponse.getContent().length() : 0,
                        elapsedMillis(requestStartNanos));
            }
            return aiResponse;
        } catch (RestClientResponseException ex) {
            logger.error("[PROVIDER] Groq HTTP failure status={} durationMs={} body={}",
                    ex.getStatusCode().value(),
                    elapsedMillis(requestStartNanos),
                    safeBody(ex.getResponseBodyAsString()), ex);
            return fallbackResponse("groq", model, FALLBACK_MESSAGE);
        } catch (ResourceAccessException ex) {
            logger.warn("[PROVIDER] Groq request could not be completed durationMs={} reason={}",
                    elapsedMillis(requestStartNanos),
                    ex.getMessage());
            return fallbackResponse("groq", model, FALLBACK_MESSAGE);
        } catch (Exception ex) {
            logger.error("[PROVIDER] Groq parsing or unexpected failure durationMs={} reason={}",
                    elapsedMillis(requestStartNanos),
                    ex.getMessage(), ex);
            return fallbackResponse("groq", model, FALLBACK_MESSAGE);
        }
    }

    private HttpEntity<Map<String, Object>> buildRequest(AiRequest request, String userMessage, String model) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(properties.getApiKey());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", buildMessages(request, userMessage));

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        if (request != null && request.getTemperature() != null) {
            generationConfig.put("temperature", request.getTemperature());
        }
        if (request != null && request.getMaxTokens() != null) {
            generationConfig.put("max_tokens", request.getMaxTokens());
        }
        body.putAll(generationConfig);

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
            addMessage(messages, "system", "Retrieved official context:\n" + String.join("\n", request.getContext()));
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

    private AiResponse toResponse(String responseBody, String model) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return fallbackResponse("groq", model, FALLBACK_MESSAGE);
        }

        JsonNode firstChoice = choices.get(0);
        String finishReason = firstChoice.path("finish_reason").isMissingNode()
                ? null
                : firstChoice.path("finish_reason").asText(null);
        String content = firstChoice.path("message").path("content").asText(null);

        if (!StringUtils.hasText(content)) {
            return fallbackResponse("groq", model, FALLBACK_MESSAGE);
        }

        return AiResponse.builder()
                .content(content.trim())
                .provider("groq")
                .model(model)
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
                : "llama-3.3-70b-versatile";
    }

    private String normalizeBaseUrl(String baseUrl) {
        String effectiveBaseUrl = StringUtils.hasText(baseUrl)
                ? baseUrl.trim()
                : "https://api.groq.com/openai/v1";
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

    private String safeBody(String body) {
        if (!StringUtils.hasText(body)) {
            return "<empty>";
        }
        return body.length() > 500 ? body.substring(0, 500) + "..." : body;
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
