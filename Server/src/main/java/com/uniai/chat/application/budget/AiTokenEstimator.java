package com.uniai.chat.application.budget;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.ConversationMemoryPromptFormatter;

import java.util.List;

/**
 * Lightweight approximate token estimator.
 * The estimate is intentionally conservative and character-based.
 */
public class AiTokenEstimator {

    private final AiContextBudgetConfiguration configuration;

    public AiTokenEstimator(AiContextBudgetConfiguration configuration) {
        this.configuration = configuration;
    }

    public long estimateTokens(String text) {
        if (!hasText(text)) {
            return 0L;
        }

        int charactersPerToken = resolveCharactersPerToken();
        return (long) Math.ceil((double) text.length() / charactersPerToken);
    }

    public long estimateTokens(AiConversationMessage message) {
        return message == null ? 0L : estimateTokens(message.getContent());
    }

    public long estimateTokens(ConversationMemory memory) {
        if (memory == null) {
            return 0L;
        }
        return estimateTokens(ConversationMemoryPromptFormatter.render(memory));
    }

    public long estimateConversationTokens(List<AiConversationMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0L;
        }

        long total = 0L;
        for (AiConversationMessage message : messages) {
            total += estimateTokens(message);
        }
        return total;
    }

    public long estimateContextTokens(List<String> contextEntries) {
        if (contextEntries == null || contextEntries.isEmpty()) {
            return 0L;
        }

        long total = 0L;
        for (String entry : contextEntries) {
            total += estimateTokens(entry);
        }
        return total;
    }

    public long estimateRequestTokens(AiRequest request) {
        if (request == null) {
            return 0L;
        }

        return estimateTokens(request.getSystemPrompt())
                + estimateTokens(request.getUserMessage())
                + estimateConversationTokens(request.getConversationHistory())
                + estimateContextTokens(request.getContext())
                + resolveOverheadTokens();
    }

    public int resolveCharactersPerToken() {
        return Math.max(1, configuration != null ? configuration.charactersPerToken() : 4);
    }

    public long resolveOverheadTokens() {
        return Math.max(0, configuration != null ? configuration.requestOverheadTokens() : 128);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
