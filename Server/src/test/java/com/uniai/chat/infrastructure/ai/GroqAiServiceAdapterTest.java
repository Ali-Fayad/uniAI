package com.uniai.chat.infrastructure.ai;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GroqAiServiceAdapterTest {

    @Test
    void generateResponseShouldMapAiRequestToGroqChatCompletionsPayload() {
        GroqAiProperties properties = new GroqAiProperties();
        properties.setApiKey("test-groq-key");
        properties.setModel("llama-3.3-70b-versatile");
        properties.setBaseUrl("https://api.groq.com/openai/v1");

        RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        GroqAiServiceAdapter adapter = new GroqAiServiceAdapter(properties, new com.fasterxml.jackson.databind.ObjectMapper(), restTemplate);

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

        server.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-groq-key"))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
                        {
                          "model": "llama-3.3-70b-versatile",
                          "messages": [
                            {"role":"system","content":"You are uniAI."},
                            {"role":"user","content":"Hi"},
                            {"role":"assistant","content":"Hello"},
                            {"role":"system","content":"Retrieved official context:\\nRetrieved official context: AUB has master programs."},
                            {"role":"user","content":"What master's programs does AUB offer?"}
                          ],
                          "temperature": 0.2,
                          "max_tokens": 256
                        }
                        """, true))
                .andRespond(withSuccess("""
                        {
                          "choices": [
                            {
                              "message": {"content": "AUB offers several master's programs."},
                              "finish_reason": "stop"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        AiResponse response = adapter.generateResponse(request);

        server.verify();
        assertFalse(response.getFallback());
        assertEquals("groq", response.getProvider());
        assertEquals("llama-3.3-70b-versatile", response.getModel());
        assertEquals("AUB offers several master's programs.", response.getContent());
        assertEquals("stop", response.getFinishReason());
    }
}
