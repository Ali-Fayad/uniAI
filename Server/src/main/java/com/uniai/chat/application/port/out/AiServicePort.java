package com.uniai.chat.application.port.out;

/**
 * Outbound port — implemented in infrastructure.
 * Decouples the chat application service from any specific AI provider.
 */
public interface AiServicePort {
    /**
     * Generates an AI response for the given user message.
     *
     * @param userMessage the content of the user's message
     * @return the AI-generated response string
     */
    String generateResponse(String userMessage);
}
