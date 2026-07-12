package com.uniai.chat.application.budget;

public record ConversationMemoryBudgetConfiguration(
        boolean enabled,
        long maxInputTokens,
        int maxOutputTokens,
        String promptPath
) {
    public ConversationMemoryBudgetConfiguration {
        maxInputTokens = Math.max(1L, maxInputTokens);
        maxOutputTokens = Math.max(1, maxOutputTokens);
        promptPath = promptPath == null || promptPath.isBlank()
                ? "prompts/conversation-memory-updater-prompt.txt"
                : promptPath.trim();
    }
}
