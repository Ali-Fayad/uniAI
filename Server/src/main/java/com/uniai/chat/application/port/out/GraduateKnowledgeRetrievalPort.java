package com.uniai.chat.application.port.out;

import com.uniai.chat.application.dto.ai.AiConversationMessage;

import java.util.List;

public interface GraduateKnowledgeRetrievalPort {

    String retrieveContext(String userMessage, List<AiConversationMessage> recentConversationHistory);
}
