package com.uniai.chat.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.memory")
public class ConversationMemoryProperties {

    private boolean enabled = true;
    private long maxInputTokens = 1200L;
    private int maxOutputTokens = 250;
    private String promptPath = "prompts/conversation-memory-updater-prompt.txt";
}
