package com.uniai.chat.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.port.out.AiServicePort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gemini-backed implementation of {@link AiServicePort}.
 * All Gemini-specific transport and response parsing stays inside infrastructure.
 */
public class GeminiAiServiceAdapter implements AiServicePort {

    private static final Logger logger = LogManager.getLogger(GeminiAiServiceAdapter.class);
    private static final String FALLBACK_MESSAGE =
            "AI service is temporarily unavailable. Please try again later.";

    private final GeminiAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public GeminiAiServiceAdapter(GeminiAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = buildRestTemplate();
    }

    @Override
    public String generateResponse(String userMessage) {
        if (!StringUtils.hasText(userMessage)) {
            return "Please enter a message.";
        }

        if (!StringUtils.hasText(properties.getApiKey())) {
            logger.warn("Gemini provider selected but ai.gemini.api-key is missing or blank");
            return "Gemini is not configured. Please set ai.gemini.api-key.";
        }

        String model = StringUtils.hasText(properties.getModel())
                ? properties.getModel().trim()
                : "gemini-2.5-flash";
        String baseUrl = normalizeBaseUrl(properties.getBaseUrl());
        String url = baseUrl + "/models/" + model + ":generateContent";

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    buildRequest(userMessage),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                logger.warn("Gemini returned an empty or non-success response");
                return FALLBACK_MESSAGE;
            }

            return extractText(response.getBody());
        } catch (RestClientResponseException ex) {
            logger.warn("Gemini request failed with status {}: {}",
                    ex.getStatusCode().value(),
                    safeBody(ex.getResponseBodyAsString()));
            return FALLBACK_MESSAGE;
        } catch (ResourceAccessException ex) {
            logger.warn("Gemini request could not be completed: {}", ex.getMessage());
            return FALLBACK_MESSAGE;
        } catch (Exception ex) {
            logger.warn("Unexpected Gemini error: {}", ex.getMessage(), ex);
            return FALLBACK_MESSAGE;
        }
    }

    private HttpEntity<Map<String, Object>> buildRequest(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("x-goog-api-key", properties.getApiKey());

        Map<String, Object> part = new LinkedHashMap<>();
        part.put("text", userMessage);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("role", "user");
        content.put("parts", List.of(part));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", List.of(content));

        return new HttpEntity<>(body, headers);
    }

    private String extractText(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray()) {
            return FALLBACK_MESSAGE;
        }

        StringBuilder output = new StringBuilder();
        for (JsonNode candidate : candidates) {
            JsonNode parts = candidate.path("content").path("parts");
            if (!parts.isArray()) {
                continue;
            }
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
        return StringUtils.hasText(text) ? text : FALLBACK_MESSAGE;
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
