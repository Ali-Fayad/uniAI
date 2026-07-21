package com.uniai.admin.prompt.service;

import com.uniai.admin.prompt.dto.AdminPromptResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class AdminPromptService {

    private record PromptDefinition(
            String key, String displayName, String description, String resourcePath,
            String caller, String operation, String expectedOutput, String riskLevel) {
    }

    private static final Map<String, PromptDefinition> PROMPTS = Map.of(
            "graduate-route-planner", new PromptDefinition(
                    "graduate-route-planner", "Graduate Route Planner",
                    "Selects an approved graduate-knowledge route and its arguments.",
                    "prompts/graduate-route-planner-prompt.txt",
                    "AiGraduateRoutePlannerAdapter", "INTERPRETATION", "Strict JSON route plan", "CRITICAL"),
            "chat-system", new PromptDefinition(
                    "chat-system", "Chat System Prompt",
                    "Controls grounded final-answer generation and response style.",
                    "prompts/chat-system-prompt.txt",
                    "ChatApplicationService", "MAIN_RESPONSE", "Markdown/natural-language answer", "HIGH"),
            "conversation-memory-updater", new PromptDefinition(
                    "conversation-memory-updater", "Conversation Memory Updater",
                    "Produces an allowlisted structured conversation-memory patch.",
                    "prompts/conversation-memory-updater-prompt.txt",
                    "AiConversationMemoryUpdateAdapter", "MEMORY_UPDATE", "Strict JSON memory patch", "CRITICAL"),
            "chat-title-generator", new PromptDefinition(
                    "chat-title-generator", "Chat Title Generator",
                    "Generates a short title from the first user message.",
                    "prompts/chat-title-generator-prompt.txt",
                    "ChatTitleGenerationManager", "TITLE_GENERATION", "Short plain-text title", "MEDIUM")
    );

    public List<AdminPromptResponse> list() {
        return PROMPTS.values().stream()
                .sorted((a, b) -> a.key().compareTo(b.key()))
                .map(definition -> toResponse(definition, null))
                .toList();
    }

    public AdminPromptResponse get(String key) {
        PromptDefinition definition = PROMPTS.get(key);
        if (definition == null) {
            throw new PromptNotFoundException();
        }
        return toResponse(definition, load(definition.resourcePath()));
    }

    private AdminPromptResponse toResponse(PromptDefinition definition, String content) {
        return new AdminPromptResponse(
                definition.key(), definition.displayName(), definition.description(),
                definition.resourcePath(), definition.caller(), definition.operation(),
                definition.expectedOutput(), definition.riskLevel(), false, content);
    }

    private String load(String resourcePath) {
        try {
            return new String(new ClassPathResource(resourcePath).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load approved prompt resource", exception);
        }
    }

    public static class PromptNotFoundException extends RuntimeException {
    }
}
