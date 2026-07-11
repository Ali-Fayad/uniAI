package com.uniai.chat.application.budget;

import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;

public record GraduateQueryInterpretationBudgetResult(
        GraduateQueryInterpretationRequest request,
        long originalEstimatedInputTokens,
        long finalEstimatedInputTokens,
        long maxInputTokens,
        long reservedOutputTokens,
        int originalHistoryCount,
        int finalHistoryCount,
        boolean historyTrimmed,
        boolean requestFits,
        String diagnosticCategory
) {
}
