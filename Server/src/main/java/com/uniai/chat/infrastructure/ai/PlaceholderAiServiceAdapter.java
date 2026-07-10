package com.uniai.chat.infrastructure.ai;

import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.port.out.AiServicePort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

/**
 * Placeholder implementation of {@link AiServicePort}.
 * Replace with a real AI provider (e.g. OpenAI, Ollama) without touching any other class.
 */
public class PlaceholderAiServiceAdapter implements AiServicePort {

    private static final Logger logger = LogManager.getLogger(PlaceholderAiServiceAdapter.class);

    @Override
    public AiResponse generateResponse(AiRequest request) {
        String userMessage = request != null ? request.getUserMessage() : null;
        logger.debug("[PROVIDER] Request received provider=placeholder messageLength={} historyCount={} contextCount={}",
                StringUtils.hasText(userMessage) ? userMessage.length() : 0,
                request != null && request.getConversationHistory() != null ? request.getConversationHistory().size() : 0,
                request != null && request.getContext() != null ? request.getContext().size() : 0);
        AiResponse response = AiResponse.builder()
                .content("AI response to: " + userMessage)
                .provider("placeholder")
                .model("placeholder")
                .fallback(false)
                .build();
        logger.debug("[PROVIDER] Request completed provider=placeholder responseLength={}",
                response.getContent() != null ? response.getContent().length() : 0);
        return response;
    }
}
