package com.uniai.chat.infrastructure.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.provider.AiProviderFailureCategory;
import com.uniai.chat.application.provider.AiProviderRuntimeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaAiServiceAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void generateResponseShouldMapAiRequestToOllamaChatPayload() {
        OllamaAiProperties properties = new OllamaAiProperties();
        properties.setBaseUrl("http://localhost:11434");
        properties.setModel("gemma3:4b");
        properties.setTimeoutSeconds(120);

        InMemoryAiProviderStatusRegistry registry = new InMemoryAiProviderStatusRegistry();
        RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        OllamaAiServiceAdapter adapter = new OllamaAiServiceAdapter(properties, objectMapper, restTemplate, registry);

        AiRequest request = AiRequest.builder()
                .systemPrompt("You are uniAI.")
                .userMessage("What master's programs does AUB offer?")
                .context(List.of("Retrieved official context: AUB has master programs."))
                .conversationHistory(List.of(
                        AiConversationMessage.builder().role("user").content("Hi").build(),
                        AiConversationMessage.builder().role("assistant").content("Hello").build()
                ))
                .temperature(0.2)
                .maxTokens(256)
                .build();

        server.expect(requestTo("http://localhost:11434/api/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(headerDoesNotExist("Authorization"))
                .andExpect(content().json("""
                        {
                          "model": "gemma3:4b",
                          "stream": false,
                          "messages": [
                            {"role":"system","content":"You are uniAI."},
                            {"role":"user","content":"Hi"},
                            {"role":"assistant","content":"Hello"},
                            {"role":"system","content":"Retrieved official graduate context:\\nRetrieved official context: AUB has master programs."},
                            {"role":"user","content":"What master's programs does AUB offer?"}
                          ],
                          "options": {
                            "temperature": 0.2,
                            "num_predict": 256
                          }
                        }
                        """, true))
                .andRespond(withSuccess("""
                        {
                          "model": "llama3.2",
                          "message": {"role": "assistant", "content": "AUB offers several master's programs."},
                          "done": true,
                          "done_reason": "stop",
                          "prompt_eval_count": 12,
                          "eval_count": 34,
                          "total_duration": 123456
                        }
                        """, MediaType.APPLICATION_JSON));

        AiResponse response = adapter.generateResponse(request);

        server.verify();
        assertFalse(response.getFallback());
        assertEquals(AiProviderFailureCategory.NONE, response.getFailureCategory());
        assertFalse(response.getRetryable());
        assertEquals("ollama", response.getProvider());
        assertEquals("llama3.2", response.getModel());
        assertEquals("AUB offers several master's programs.", response.getContent());
        assertEquals("stop", response.getFinishReason());
        assertEquals(AiProviderRuntimeStatus.AVAILABLE, registry.getStatus("ollama").status());
    }

    @Test
    void generateResponseShouldFallbackWhenResponseHasNoUsableContent() {
        OllamaAiProperties properties = new OllamaAiProperties();
        properties.setBaseUrl("http://localhost:11434");
        properties.setModel("gemma3:4b");
        properties.setTimeoutSeconds(120);

        InMemoryAiProviderStatusRegistry registry = new InMemoryAiProviderStatusRegistry();
        RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        OllamaAiServiceAdapter adapter = new OllamaAiServiceAdapter(properties, objectMapper, restTemplate, registry);

        AiRequest request = AiRequest.builder()
                .systemPrompt("You are uniAI.")
                .userMessage("Hello")
                .build();

        server.expect(requestTo("http://localhost:11434/api/chat"))
                .andRespond(withSuccess("""
                        {
                          "model": "gemma3:4b",
                          "message": {"role": "assistant", "content": ""},
                          "done": true,
                          "done_reason": "stop"
                        }
                        """, MediaType.APPLICATION_JSON));

        AiResponse response = adapter.generateResponse(request);

        server.verify();
        assertTrue(response.getFallback());
        assertEquals(AiProviderFailureCategory.EMPTY_RESPONSE, response.getFailureCategory());
        assertTrue(response.getRetryable());
        assertEquals("ollama", response.getProvider());
        assertEquals("gemma3:4b", response.getModel());
        assertTrue(response.getContent().contains("temporarily unavailable"));
        assertEquals(AiProviderRuntimeStatus.UNAVAILABLE, registry.getStatus("ollama").status());
    }

    @Test
    void generateResponseShouldFallbackWhenOllamaIsUnavailable() {
        OllamaAiProperties properties = new OllamaAiProperties();
        properties.setBaseUrl("http://localhost:11434");
        properties.setModel("gemma3:4b");
        properties.setTimeoutSeconds(120);

        RestTemplate restTemplate = new ThrowingRestTemplate();
        InMemoryAiProviderStatusRegistry registry = new InMemoryAiProviderStatusRegistry();
        OllamaAiServiceAdapter adapter = new OllamaAiServiceAdapter(properties, objectMapper, restTemplate, registry);

        AiRequest request = AiRequest.builder()
                .systemPrompt("You are uniAI.")
                .userMessage("Hello")
                .build();

        AiResponse response = adapter.generateResponse(request);

        assertTrue(response.getFallback());
        assertEquals(AiProviderFailureCategory.UNAVAILABLE, response.getFailureCategory());
        assertTrue(response.getRetryable());
        assertEquals("ollama", response.getProvider());
        assertEquals("gemma3:4b", response.getModel());
        assertTrue(response.getContent().contains("temporarily unavailable"));
        assertEquals(AiProviderRuntimeStatus.UNAVAILABLE, registry.getStatus("ollama").status());
    }

    private static class ThrowingRestTemplate extends RestTemplate {
        @Override
        public <T> org.springframework.http.ResponseEntity<T> exchange(
                String url,
                HttpMethod method,
                org.springframework.http.HttpEntity<?> requestEntity,
                Class<T> responseType,
                Object... uriVariables
        ) {
            throw new ResourceAccessException("Connection refused");
        }
    }
}
