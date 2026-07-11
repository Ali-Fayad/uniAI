package com.uniai.chat.infrastructure.prompt;

import com.uniai.chat.application.port.out.GraduateQueryInterpreterPromptPort;
import com.uniai.chat.infrastructure.config.GraduateQueryInterpretationProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class GraduateQueryInterpreterPromptProvider implements GraduateQueryInterpreterPromptPort {

    private static final Logger logger = LogManager.getLogger(GraduateQueryInterpreterPromptProvider.class);

    private final GraduateQueryInterpretationProperties properties;
    private final String prompt;

    public GraduateQueryInterpreterPromptProvider(GraduateQueryInterpretationProperties properties) {
        this.properties = properties;
        this.prompt = loadPrompt();
    }

    @Override
    public String getPrompt() {
        return prompt;
    }

    private String loadPrompt() {
        String promptPath = properties != null && properties.getPromptPath() != null && !properties.getPromptPath().isBlank()
                ? properties.getPromptPath().trim()
                : "prompts/graduate-query-interpreter-prompt.txt";

        ClassPathResource resource = new ClassPathResource(promptPath);
        try {
            byte[] bytes = resource.getInputStream().readAllBytes();
            String prompt = new String(bytes, StandardCharsets.UTF_8).trim();
            logger.info("[PROMPT] Graduate query interpreter prompt loaded path={} size={}", promptPath, prompt.length());
            return prompt;
        } catch (IOException ex) {
            logger.error("[PROMPT] Failed to load graduate query interpreter prompt path={} reason={}", promptPath, ex.getMessage(), ex);
            throw new IllegalStateException("Failed to load graduate query interpreter prompt from " + promptPath, ex);
        }
    }
}
