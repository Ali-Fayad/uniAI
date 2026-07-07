package com.uniai.chat.application.port.out;

import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;

/**
 * Outbound port — implemented in infrastructure.
 * Decouples the chat application service from any specific AI provider.
 */
public interface AiServicePort {
    /**
     * Generates an AI response for the given request.
     *
     * @param request the AI request
     * @return the AI-generated response payload
     */
    AiResponse generateResponse(AiRequest request);
}
