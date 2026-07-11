package com.uniai.chat.application.budget;

public record GraduateQueryInterpretationBudgetConfiguration(
        boolean enabled,
        long maxInputTokens,
        int maxOutputTokens,
        int historyMessageLimit,
        String promptPath
) {
    public GraduateQueryInterpretationBudgetConfiguration {
        maxInputTokens = Math.max(1L, maxInputTokens);
        maxOutputTokens = Math.max(1, maxOutputTokens);
        historyMessageLimit = Math.max(0, historyMessageLimit);
        promptPath = promptPath == null || promptPath.isBlank()
                ? "prompts/graduate-query-interpreter-prompt.txt"
                : promptPath.trim();
    }
}
