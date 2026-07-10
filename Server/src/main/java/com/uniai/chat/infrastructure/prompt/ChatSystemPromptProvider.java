package com.uniai.chat.infrastructure.prompt;

import com.uniai.chat.application.port.out.ChatSystemPromptPort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ChatSystemPromptProvider implements ChatSystemPromptPort {

    private static final Logger logger = LogManager.getLogger(ChatSystemPromptProvider.class);
    private static final String PROMPT_PATH = "prompts/chat-system-prompt.txt";

    private final String prompt;

    public ChatSystemPromptProvider() {
        this.prompt = loadPrompt();
    }

    @Override
    public String getPrompt() {
        return prompt;
    }

    private String loadPrompt() {
        ClassPathResource resource = new ClassPathResource(PROMPT_PATH);
        try {
            byte[] bytes = resource.getInputStream().readAllBytes();
            String prompt = new String(bytes, StandardCharsets.UTF_8).trim();
            logger.info("[PROMPT] Chat system prompt loaded path={} size={}", PROMPT_PATH, prompt.length());
            return prompt;
        } catch (IOException ex) {
            logger.error("[PROMPT] Failed to load chat system prompt path={} reason={}", PROMPT_PATH, ex.getMessage(), ex);
            throw new IllegalStateException("Failed to load chat system prompt from " + PROMPT_PATH, ex);
        }
    }
}
