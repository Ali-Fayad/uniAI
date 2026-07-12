package com.uniai.chat.application.interpretation;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.memory.ConversationMemory;

import java.util.List;

public record GraduateQueryInterpretationRequest(
        String userMessage,
        List<AiConversationMessage> recentConversationHistory,
        ConversationMemory conversationMemory
) {
    public GraduateQueryInterpretationRequest {
        recentConversationHistory = recentConversationHistory == null ? List.of() : List.copyOf(recentConversationHistory);
        conversationMemory = conversationMemory == null ? ConversationMemory.empty() : conversationMemory;
    }
}
