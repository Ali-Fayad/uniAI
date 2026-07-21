package com.uniai.admin.prompt.dto;

public record AdminPromptResponse(
        String key,
        String displayName,
        String description,
        String resourcePath,
        String caller,
        String operation,
        String expectedOutput,
        String riskLevel,
        boolean editable,
        String content) {
}
