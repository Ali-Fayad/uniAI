package com.uniai.chat.infrastructure.ai;

import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.port.out.AiServicePort;

/**
 * Placeholder implementation of {@link AiServicePort}.
 * Replace with a real AI provider (e.g. OpenAI, Ollama) without touching any other class.
 */
public class PlaceholderAiServiceAdapter implements AiServicePort {

    @Override
    public AiResponse generateResponse(AiRequest request) {
        String userMessage = request != null ? request.getUserMessage() : null;
        return AiResponse.builder()
                .content("AI response to: " + userMessage)
                .provider("placeholder")
                .model("placeholder")
                .fallback(false)
                .build();
    }
}
