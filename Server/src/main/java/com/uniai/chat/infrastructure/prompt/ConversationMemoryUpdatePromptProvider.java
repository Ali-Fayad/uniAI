package com.uniai.chat.infrastructure.prompt;

import com.uniai.chat.application.port.out.ConversationMemoryPromptPort;
import com.uniai.chat.infrastructure.config.ConversationMemoryProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ConversationMemoryUpdatePromptProvider implements ConversationMemoryPromptPort {

    private static final Logger logger = LogManager.getLogger(ConversationMemoryUpdatePromptProvider.class);

    private final String prompt;

    public ConversationMemoryUpdatePromptProvider(ConversationMemoryProperties properties) {
        this.prompt = loadPrompt(properties);
    }

    @Override
    public String getPrompt() {
        return prompt;
    }

    private String loadPrompt(ConversationMemoryProperties properties) {
        String promptPath = properties != null && properties.getPromptPath() != null && !properties.getPromptPath().isBlank()
                ? properties.getPromptPath().trim()
                : "prompts/conversation-memory-updater-prompt.txt";

        ClassPathResource resource = new ClassPathResource(promptPath);
        try {
            byte[] bytes = resource.getInputStream().readAllBytes();
            String prompt = new String(bytes, StandardCharsets.UTF_8).trim();
            logger.info("[PROMPT] Conversation memory updater prompt loaded path={} size={}", promptPath, prompt.length());
            return prompt;
        } catch (IOException ex) {
            logger.error("[PROMPT] Failed to load conversation memory updater prompt path={} reason={}", promptPath, ex.getMessage(), ex);
            throw new IllegalStateException("Failed to load conversation memory updater prompt from " + promptPath, ex);
        }
    }
}
