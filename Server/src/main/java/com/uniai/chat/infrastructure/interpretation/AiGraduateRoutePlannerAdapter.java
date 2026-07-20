package com.uniai.chat.infrastructure.interpretation;

import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetConfiguration;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.dto.ai.AiOperation;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationProviderException;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.ConversationMemoryPromptFormatter;
import com.uniai.chat.application.planning.GraduateRoutePlanParser;
import com.uniai.chat.application.planning.GraduateRoutePlanningException;
import com.uniai.chat.application.planning.ValidatedGraduateRoutePlan;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.port.out.GraduateRoutePlannerPort;
import com.uniai.chat.application.port.out.GraduateRoutePlannerPromptPort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import java.util.List;

/** AI adapter that selects one route. Validation remains entirely Java-owned. */
public final class AiGraduateRoutePlannerAdapter implements GraduateRoutePlannerPort {
    private static final Logger logger = LogManager.getLogger(AiGraduateRoutePlannerAdapter.class);
    private final AiServicePort aiServicePort;
    private final GraduateRoutePlannerPromptPort promptPort;
    private final GraduateQueryInterpretationBudgetConfiguration budgetConfiguration;
    private final GraduateRoutePlanParser parser;

    public AiGraduateRoutePlannerAdapter(AiServicePort aiServicePort,
                                         GraduateRoutePlannerPromptPort promptPort,
                                         GraduateQueryInterpretationBudgetConfiguration budgetConfiguration,
                                         GraduateRoutePlanParser parser) {
        this.aiServicePort = aiServicePort;
        this.promptPort = promptPort;
        this.budgetConfiguration = budgetConfiguration;
        this.parser = parser;
    }

    @Override
    public ValidatedGraduateRoutePlan<?> plan(GraduateQueryInterpretationRequest request) {
        AiResponse response = requestProvider(request);
        if (!StringUtils.hasText(response.getContent())) {
            throw providerFailure("AI_QUERY_PLANNER_PROVIDER_EMPTY", "Route planner provider returned empty content");
        }
        try {
            // Markdown fences are intentionally not stripped: the contract requires one JSON object only.
            return parser.parse(response.getContent().trim());
        } catch (GraduateRoutePlanningException ex) {
            throw new GraduateQueryInterpretationProviderException(
                    "Route planner returned an invalid contract",
                    "AI_QUERY_PLANNER_PROVIDER_INVALID",
                    ex);
        }
    }

    private AiResponse requestProvider(GraduateQueryInterpretationRequest request) {
        String userMessage = request != null ? request.userMessage() : null;
        List<AiConversationMessage> history = request != null ? request.recentConversationHistory() : List.of();
        ConversationMemory memory = request != null ? request.conversationMemory() : ConversationMemory.empty();
        String prompt = appendMemory(promptPort.getPrompt(), memory);

        AiRequest aiRequest = AiRequest.builder()
                .systemPrompt(prompt)
                .userMessage(userMessage)
                .conversationHistory(history)
                .conversationMemory(memory)
                .operation(AiOperation.INTERPRETATION)
                .temperature(0.0)
                .maxTokens(budgetConfiguration != null ? budgetConfiguration.maxOutputTokens() : 500)
                .build();
        AiResponse response = aiServicePort.generateResponse(aiRequest);
        if (response == null || Boolean.TRUE.equals(response.getFallback())) {
            throw providerFailure("AI_QUERY_PLANNER_PROVIDER_UNAVAILABLE", "Route planner provider is unavailable");
        }
        if ("MAX_TOKENS".equalsIgnoreCase(response.getFinishReason())
                || "LENGTH".equalsIgnoreCase(response.getFinishReason())) {
            logger.warn("[AI_ROUTE_PLANNER] Provider response truncated provider={} model={} finishReason={}",
                    response.getProvider(), response.getModel(), response.getFinishReason());
            throw providerFailure("AI_QUERY_PLANNER_PROVIDER_TRUNCATED", "Route planner response was truncated");
        }
        logger.debug("[AI_ROUTE_PLANNER] Plan received provider={} model={} historyCount={} maxTokens={}",
                response.getProvider(), response.getModel(), history.size(), aiRequest.getMaxTokens());
        return response;
    }

    private String appendMemory(String prompt, ConversationMemory memory) {
        String rendered = ConversationMemoryPromptFormatter.render(memory);
        return StringUtils.hasText(rendered) ? prompt + "\n\nTrusted conversation memory:\n" + rendered : prompt;
    }

    private GraduateQueryInterpretationProviderException providerFailure(String category, String message) {
        return new GraduateQueryInterpretationProviderException(message, category);
    }
}
