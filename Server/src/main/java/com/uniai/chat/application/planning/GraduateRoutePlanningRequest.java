package com.uniai.chat.application.planning;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.memory.ConversationMemory;

import java.util.List;

public record GraduateRoutePlanningRequest(
        String userMessage,
        List<AiConversationMessage> recentConversationHistory,
        ConversationMemory conversationMemory
) {
    public GraduateRoutePlanningRequest {
        recentConversationHistory = recentConversationHistory == null ? List.of() : List.copyOf(recentConversationHistory);
        conversationMemory = conversationMemory == null ? ConversationMemory.empty() : conversationMemory;
    }
}
