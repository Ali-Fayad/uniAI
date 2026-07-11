package com.uniai.chat.application.interpretation;

import com.uniai.chat.application.dto.ai.AiConversationMessage;

import java.util.List;

public record GraduateQueryInterpretationRequest(
        String userMessage,
        List<AiConversationMessage> recentConversationHistory
) {
    public GraduateQueryInterpretationRequest {
        recentConversationHistory = recentConversationHistory == null ? List.of() : List.copyOf(recentConversationHistory);
    }
}
