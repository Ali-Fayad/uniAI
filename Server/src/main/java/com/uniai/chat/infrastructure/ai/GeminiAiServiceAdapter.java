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
 * Gemini-backed implementation of {@link AiServicePort}.
 * All Gemini-specific transport and response parsing stays inside infrastructure.
 */
public class GeminiAiServiceAdapter implements AiServicePort {

    private static final Logger logger = LogManager.getLogger(GeminiAiServiceAdapter.class);
    private static final String FALLBACK_MESSAGE = "AI service error : this message is from GeminiAiServiceAdapter. Please try again later.";

    private final GeminiAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public GeminiAiServiceAdapter(GeminiAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = buildRestTemplate();
    }

    @Override
    public AiResponse generateResponse(AiRequest request) {
        String userMessage = request != null ? request.getUserMessage() : null;
        if (!StringUtils.hasText(userMessage)) {
            return fallbackResponse("placeholder", "placeholder", "Please enter a message.");
        }

        if (!StringUtils.hasText(properties.getApiKey())) {
            logger.warn("Gemini provider selected but ai.gemini.api-key is missing or blank");
            return fallbackResponse("gemini", resolveModel(), "Gemini is not configured. Please set ai.gemini.api-key.");
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
                logger.warn("Gemini returned an empty or non-success response");
                return fallbackResponse("gemini", model, FALLBACK_MESSAGE);
            }

            return toResponse(response.getBody(), model);
        } catch (RestClientResponseException ex) {
            logger.error("Gemini Status: {}", ex.getStatusCode());
            logger.error("Gemini Response: {}", ex.getResponseBodyAsString(), ex);

            return fallbackResponse("gemini", model, FALLBACK_MESSAGE);
        } catch (ResourceAccessException ex) {
            logger.warn("Gemini request could not be completed: {}", ex.getMessage());
            return fallbackResponse("gemini", model, FALLBACK_MESSAGE);
        } catch (Exception ex) {
            logger.warn("Unexpected Gemini error: {}", ex.getMessage(), ex);
            return fallbackResponse("gemini", model, FALLBACK_MESSAGE);
        }
    }

    private HttpEntity<Map<String, Object>> buildRequest(AiRequest request, String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("x-goog-api-key", properties.getApiKey());

        Map<String, Object> body = new LinkedHashMap<>();

        String systemPrompt = request != null ? request.getSystemPrompt() : null;

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

    private AiResponse toResponse(String responseBody, String model) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return fallbackResponse("gemini", model, FALLBACK_MESSAGE);
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
            return fallbackResponse("gemini", model, FALLBACK_MESSAGE);
        }

        return AiResponse.builder()
                .content(text)
                .provider("gemini")
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

    private RestTemplate buildRestTemplate() {
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
}
