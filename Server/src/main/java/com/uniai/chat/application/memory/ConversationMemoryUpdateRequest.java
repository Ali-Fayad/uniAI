package com.uniai.chat.application.memory;

import com.uniai.chat.application.planning.GraduateRouteExecutionResult;

public record ConversationMemoryUpdateRequest(
        ConversationMemory previousMemory,
        String currentUserMessage,
        String assistantResponse,
        GraduateRouteExecutionResult routeResult
) {
    public ConversationMemoryUpdateRequest {
        previousMemory = previousMemory == null ? ConversationMemory.empty() : previousMemory;
        currentUserMessage = currentUserMessage == null ? "" : currentUserMessage;
        assistantResponse = assistantResponse == null ? "" : assistantResponse;
    }
}
