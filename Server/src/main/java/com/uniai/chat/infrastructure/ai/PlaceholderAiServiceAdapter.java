package com.uniai.chat.infrastructure.ai;

import com.uniai.chat.application.port.out.AiServicePort;

/**
 * Placeholder implementation of {@link AiServicePort}.
 * Replace with a real AI provider (e.g. OpenAI, Ollama) without touching any other class.
 */
public class PlaceholderAiServiceAdapter implements AiServicePort {

    @Override
    public String generateResponse(String userMessage) {
        return "AI response to: " + userMessage;
    }
}
