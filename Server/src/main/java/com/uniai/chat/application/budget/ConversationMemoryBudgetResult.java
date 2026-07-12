package com.uniai.chat.application.budget;

import com.uniai.chat.application.memory.ConversationMemoryUpdateRequest;

public record ConversationMemoryBudgetResult(
        ConversationMemoryUpdateRequest request,
        long originalEstimatedInputTokens,
        long finalEstimatedInputTokens,
        long maxInputTokens,
        long reservedOutputTokens,
        boolean requestFits,
        String diagnosticCategory
) {
}
