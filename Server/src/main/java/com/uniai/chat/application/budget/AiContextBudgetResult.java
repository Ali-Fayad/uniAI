package com.uniai.chat.application.budget;

import com.uniai.chat.application.dto.ai.AiRequest;

public record AiContextBudgetResult(
        AiRequest request,
        long originalEstimatedInputTokens,
        long finalEstimatedInputTokens,
        long maxInputTokens,
        long reservedOutputTokens,
        int originalHistoryCount,
        int finalHistoryCount,
        int originalContextCount,
        int finalContextCount,
        boolean historyTrimmed,
        boolean contextTrimmed,
        boolean requestFits,
        String activeProvider,
        String diagnosticCategory
) {
}
