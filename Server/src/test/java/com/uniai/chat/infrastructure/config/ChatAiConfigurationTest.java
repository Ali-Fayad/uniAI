package com.uniai.chat.infrastructure.config;

import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.infrastructure.ai.GeminiAiProperties;
import com.uniai.chat.infrastructure.ai.GroqAiProperties;
import com.uniai.chat.infrastructure.ai.GroqAiServiceAdapter;
import com.uniai.chat.infrastructure.ai.OllamaAiProperties;
import com.uniai.chat.infrastructure.ai.OllamaAiServiceAdapter;
import com.uniai.chat.infrastructure.ai.PlaceholderAiServiceAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ChatAiConfigurationTest {

    private final ChatAiConfiguration configuration = new ChatAiConfiguration();

    @Test
    void aiServicePortShouldSelectOllamaProvider() {
        GeminiAiProperties gemini = new GeminiAiProperties();
        GroqAiProperties groq = new GroqAiProperties();
        OllamaAiProperties ollama = new OllamaAiProperties();

        AiServicePort aiServicePort = configuration.aiServicePort("ollama", gemini, groq, ollama);

        assertInstanceOf(OllamaAiServiceAdapter.class, aiServicePort);
    }

    @Test
    void aiServicePortShouldFallBackToPlaceholderForUnknownProvider() {
        GeminiAiProperties gemini = new GeminiAiProperties();
        GroqAiProperties groq = new GroqAiProperties();
        OllamaAiProperties ollama = new OllamaAiProperties();

        AiServicePort aiServicePort = configuration.aiServicePort("not-a-provider", gemini, groq, ollama);

        assertInstanceOf(PlaceholderAiServiceAdapter.class, aiServicePort);
    }
}
