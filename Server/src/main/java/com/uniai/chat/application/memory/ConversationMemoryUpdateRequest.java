package com.uniai.chat.application.memory;

import com.uniai.chat.application.interpretation.GraduateQueryInterpretationResult;

public record ConversationMemoryUpdateRequest(
        ConversationMemory previousMemory,
        String currentUserMessage,
        String assistantResponse,
        GraduateQueryInterpretationResult interpretationResult
) {
    public ConversationMemoryUpdateRequest {
        previousMemory = previousMemory == null ? ConversationMemory.empty() : previousMemory;
        currentUserMessage = currentUserMessage == null ? "" : currentUserMessage;
        assistantResponse = assistantResponse == null ? "" : assistantResponse;
    }
}
