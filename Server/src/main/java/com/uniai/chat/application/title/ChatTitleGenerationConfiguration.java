package com.uniai.chat.application.title;

public record ChatTitleGenerationConfiguration(
        boolean enabled,
        long maxInputTokens,
        int maxOutputTokens,
        int maxTitleLength
) {
    public ChatTitleGenerationConfiguration {
        maxInputTokens = Math.max(1L, maxInputTokens);
        maxOutputTokens = Math.max(1, maxOutputTokens);
        maxTitleLength = Math.max(1, maxTitleLength);
    }
}
