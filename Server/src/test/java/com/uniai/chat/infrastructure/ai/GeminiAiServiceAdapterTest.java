package com.uniai.chat.infrastructure.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.provider.AiProviderFailureCategory;
import com.uniai.chat.application.provider.AiProviderRuntimeStatus;
import com.uniai.chat.application.provider.AiProviderStatusSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiAiServiceAdapterTest {

    @Test
    void generateResponseShouldRecordSuccess() {
        GeminiAiProperties properties = new GeminiAiProperties();
        properties.setApiKey("test-gemini-key");
        properties.setModel("gemini-2.5-flash");
        properties.setBaseUrl("https://generativelanguage.googleapis.com/v1beta");

        InMemoryAiProviderStatusRegistry registry = new InMemoryAiProviderStatusRegistry();
        RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        GeminiAiServiceAdapter adapter = new GeminiAiServiceAdapter(properties, new ObjectMapper(), restTemplate, registry);

        AiRequest request = AiRequest.builder()
                .userMessage("Hello")
                .systemPrompt("You are uniAI.")
                .build();

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-gemini-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("""
                        {
                          "candidates": [
                            {
                              "content": { "parts": [ { "text": "Hello there." } ] },
                              "finishReason": "STOP"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        AiResponse response = adapter.generateResponse(request);

        server.verify();
        assertFalse(response.getFallback());
        assertEquals(AiProviderFailureCategory.NONE, response.getFailureCategory());
        assertFalse(response.getRetryable());
        AiProviderStatusSnapshot snapshot = registry.getStatus("gemini");
        assertEquals(AiProviderRuntimeStatus.AVAILABLE, snapshot.status());
        assertEquals(AiProviderFailureCategory.NONE, snapshot.lastFailureCategory());
    }

    @Test
    void generateResponseShouldRecordMissingConfiguration() {
        GeminiAiProperties properties = new GeminiAiProperties();
        properties.setApiKey("");
        properties.setModel("gemini-2.5-flash");
        properties.setBaseUrl("https://generativelanguage.googleapis.com/v1beta");

        InMemoryAiProviderStatusRegistry registry = new InMemoryAiProviderStatusRegistry();
        GeminiAiServiceAdapter adapter = new GeminiAiServiceAdapter(properties, new ObjectMapper(), registry);

        AiResponse response = adapter.generateResponse(AiRequest.builder().userMessage("Hello").build());

        assertTrue(response.getFallback());
        assertEquals(AiProviderFailureCategory.MISCONFIGURED, response.getFailureCategory());
        assertFalse(response.getRetryable());
        assertEquals(AiProviderRuntimeStatus.MISCONFIGURED, registry.getStatus("gemini").status());
    }

    @Test
    void generateResponseShouldClassifyRateLimitAndServerError() {
        GeminiAiProperties properties = new GeminiAiProperties();
        properties.setApiKey("test-gemini-key");
        properties.setModel("gemini-2.5-flash");

        InMemoryAiProviderStatusRegistry registry = new InMemoryAiProviderStatusRegistry();
        RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        GeminiAiServiceAdapter adapter = new GeminiAiServiceAdapter(properties, new ObjectMapper(), restTemplate, registry);

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).contentType(MediaType.APPLICATION_JSON).body("{}"));

        AiResponse rateLimited = adapter.generateResponse(AiRequest.builder().userMessage("Hello").build());
        assertEquals(AiProviderFailureCategory.RATE_LIMITED, rateLimited.getFailureCategory());
        assertTrue(rateLimited.getRetryable());
        assertEquals(AiProviderRuntimeStatus.UNAVAILABLE, registry.getStatus("gemini").status());
        server.verify();

        RestTemplate serverErrorTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
        MockRestServiceServer serverError = MockRestServiceServer.bindTo(serverErrorTemplate).build();
        GeminiAiServiceAdapter serverErrorAdapter = new GeminiAiServiceAdapter(properties, new ObjectMapper(), serverErrorTemplate, registry);
        serverError.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body("{}"));

        AiResponse serverFailure = serverErrorAdapter.generateResponse(AiRequest.builder().userMessage("Hello").build());
        assertEquals(AiProviderFailureCategory.HTTP_SERVER_ERROR, serverFailure.getFailureCategory());
        assertTrue(serverFailure.getRetryable());
        assertEquals(AiProviderRuntimeStatus.UNAVAILABLE, registry.getStatus("gemini").status());
        serverError.verify();
    }

    @Test
    void generateResponseShouldClassifyParseAndTimeoutFailures() {
        GeminiAiProperties properties = new GeminiAiProperties();
        properties.setApiKey("test-gemini-key");
        properties.setModel("gemini-2.5-flash");

        InMemoryAiProviderStatusRegistry registry = new InMemoryAiProviderStatusRegistry();
        RestTemplate malformedTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
        MockRestServiceServer malformedServer = MockRestServiceServer.bindTo(malformedTemplate).build();
        GeminiAiServiceAdapter malformedAdapter = new GeminiAiServiceAdapter(properties, new ObjectMapper(), malformedTemplate, registry);
        malformedServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));

        AiResponse invalid = malformedAdapter.generateResponse(AiRequest.builder().userMessage("Hello").build());
        assertEquals(AiProviderFailureCategory.INVALID_RESPONSE, invalid.getFailureCategory());
        assertEquals(AiProviderRuntimeStatus.UNAVAILABLE, registry.getStatus("gemini").status());
        malformedServer.verify();

        GeminiAiServiceAdapter timeoutAdapter = new GeminiAiServiceAdapter(properties, new ObjectMapper(), new ThrowingRestTemplate(new ResourceAccessException("timeout", new SocketTimeoutException("Read timed out"))), registry);
        AiResponse timeout = timeoutAdapter.generateResponse(AiRequest.builder().userMessage("Hello").build());
        assertEquals(AiProviderFailureCategory.TIMEOUT, timeout.getFailureCategory());
        assertTrue(timeout.getRetryable());
        assertEquals(AiProviderRuntimeStatus.UNAVAILABLE, registry.getStatus("gemini").status());
    }

    @Test
    void generateResponseShouldClassifyEmptyContent() {
        GeminiAiProperties properties = new GeminiAiProperties();
        properties.setApiKey("test-gemini-key");
        properties.setModel("gemini-2.5-flash");

        InMemoryAiProviderStatusRegistry registry = new InMemoryAiProviderStatusRegistry();
        RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        GeminiAiServiceAdapter adapter = new GeminiAiServiceAdapter(properties, new ObjectMapper(), restTemplate, registry);

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andRespond(withSuccess("""
                        {
                          "candidates": [
                            {
                              "content": { "parts": [ { "text": "" } ] }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        AiResponse response = adapter.generateResponse(AiRequest.builder().userMessage("Hello").build());
        assertEquals(AiProviderFailureCategory.EMPTY_RESPONSE, response.getFailureCategory());
        assertTrue(response.getRetryable());
        assertEquals(AiProviderRuntimeStatus.UNAVAILABLE, registry.getStatus("gemini").status());
        server.verify();
    }

    private static final class ThrowingRestTemplate extends RestTemplate {
        private final RuntimeException failure;

        private ThrowingRestTemplate(RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        public <T> org.springframework.http.ResponseEntity<T> exchange(
                String url,
                HttpMethod method,
                org.springframework.http.HttpEntity<?> requestEntity,
                Class<T> responseType,
                Object... uriVariables
        ) {
            throw failure;
        }
    }
}
