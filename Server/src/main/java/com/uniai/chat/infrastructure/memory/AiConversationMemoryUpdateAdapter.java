package com.uniai.chat.infrastructure.memory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiOperation;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.memory.ConversationMemoryPromptFormatter;
import com.uniai.chat.application.memory.ConversationMemoryPatch;
import com.uniai.chat.application.memory.ConversationMemoryUpdatePort;
import com.uniai.chat.application.memory.ConversationMemoryUpdateRequest;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.port.out.ConversationMemoryPromptPort;
import com.uniai.chat.application.budget.ConversationMemoryBudgetConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AiConversationMemoryUpdateAdapter implements ConversationMemoryUpdatePort {

    private static final Logger logger = LogManager.getLogger(AiConversationMemoryUpdateAdapter.class);

    private final AiServicePort aiServicePort;
    private final ConversationMemoryPromptPort promptPort;
    private final int maxTokens;
    private final ObjectMapper objectMapper;

    public AiConversationMemoryUpdateAdapter(
            AiServicePort aiServicePort,
            ConversationMemoryPromptPort promptPort,
            ConversationMemoryBudgetConfiguration budgetConfiguration,
            ObjectMapper objectMapper
    ) {
        this.aiServicePort = aiServicePort;
        this.promptPort = promptPort;
        this.maxTokens = budgetConfiguration != null ? budgetConfiguration.maxOutputTokens() : 250;
        this.objectMapper = objectMapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ConversationMemoryPatch proposeUpdate(ConversationMemoryUpdateRequest request) {
        String prompt = promptPort != null ? promptPort.getPrompt() : "";
        String memoryText = ConversationMemoryPromptFormatter.render(request.previousMemory());
        AiRequest aiRequest = AiRequest.builder()
                .systemPrompt(prompt)
                .userMessage(request.currentUserMessage())
                .context(buildContext(memoryText, request))
                .operation(AiOperation.MEMORY_UPDATE)
                .maxTokens(maxTokens)
                .build();

        AiResponse response = aiServicePort.generateResponse(aiRequest);
        if (response == null || Boolean.TRUE.equals(response.getFallback()) || !StringUtils.hasText(response.getContent())) {
            throw new IllegalStateException("Memory update provider failed");
        }

        String json = stripMarkdownFences(response.getContent());
        try {
            ConversationMemoryPatch patch = objectMapper.readValue(json, ConversationMemoryPatch.class);
            logger.debug("[AI_MEMORY] Memory patch parsed provider={} model={} patchType={} length={}",
                    response.getProvider(),
                    response.getModel(),
                    patch != null ? patch.getClass().getSimpleName() : "null",
                    json.length());
            return patch;
        } catch (Exception ex) {
            logger.warn("[AI_MEMORY] Memory patch parse failed provider={} model={} reason={}",
                    response.getProvider(),
                    response.getModel(),
                    ex.getMessage());
            throw new IllegalStateException("Memory update response could not be parsed", ex);
        }
    }

    private List<String> buildContext(String memoryText, ConversationMemoryUpdateRequest request) {
        List<String> context = new ArrayList<>();
        if (StringUtils.hasText(memoryText)) {
            context.add("Previous trusted memory:\n" + memoryText);
        }
        if (request.routeResult() != null) {
            context.add("Validated interpretation:\n"
                    + "route=" + request.routeResult().route()
                    + "\nresolvedUniversities=" + request.routeResult().resolvedUniversities().size()
                    + "\nempty=" + request.routeResult().empty());
        }
        if (StringUtils.hasText(request.assistantResponse())) {
            context.add("Final assistant response:\n" + request.assistantResponse());
        }
        return context;
    }

    private String stripMarkdownFences(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewLine = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewLine >= 0 && lastFence > firstNewLine) {
                return trimmed.substring(firstNewLine + 1, lastFence).trim();
            }
        }
        return trimmed;
    }
}
