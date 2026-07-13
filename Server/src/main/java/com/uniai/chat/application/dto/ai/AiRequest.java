package com.uniai.chat.application.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.uniai.chat.application.memory.ConversationMemory;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequest {

    private String userMessage;
    private String systemPrompt;

    @Builder.Default
    private List<String> context = new ArrayList<>();

    @Builder.Default
    private List<AiConversationMessage> conversationHistory = new ArrayList<>();

    private ConversationMemory conversationMemory;

    @Builder.Default
    private AiOperation operation = AiOperation.UNKNOWN;

    private Double temperature;
    private Integer maxTokens;
}
