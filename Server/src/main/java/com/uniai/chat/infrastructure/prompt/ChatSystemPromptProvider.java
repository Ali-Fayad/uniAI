package com.uniai.chat.infrastructure.prompt;

import com.uniai.chat.application.port.out.ChatSystemPromptPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ChatSystemPromptProvider implements ChatSystemPromptPort {

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
            return new String(bytes, StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load chat system prompt from " + PROMPT_PATH, ex);
        }
    }
}
