package com.uniai.chat.application.budget;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateQueryInterpretationBudgetTest {

    @Test
    void budgetShouldTrimOldestHistoryFirst() {
        GraduateQueryInterpretationBudgetManager manager = manager(60, 20, 2, "gemini", 0);
        GraduateQueryInterpretationRequest request = new GraduateQueryInterpretationRequest(
                "What programs does AUB offer?",
                List.of(
                        msg("user", "one one one one"),
                        msg("assistant", "two two two two"),
                        msg("user", "three three three three"),
                        msg("assistant", "four four four four")
                ),
                ConversationMemory.empty()
        );

        GraduateQueryInterpretationBudgetResult result = manager.budget(request, "interpretation prompt");

        assertTrue(result.requestFits());
        assertTrue(result.historyTrimmed());
        assertEquals(2, result.finalHistoryCount());
        assertEquals(List.of("three three three three", "four four four four"),
                result.request().recentConversationHistory().stream().map(AiConversationMessage::getContent).toList());
    }

    @Test
    void budgetShouldRejectImpossibleRequests() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        GraduateQueryInterpretationBudgetManager manager = manager(5, 2, 4, "gemini", 0, meterRegistry);
        GraduateQueryInterpretationRequest request = new GraduateQueryInterpretationRequest(
                "What programs does AUB offer?",
                List.of(),
                ConversationMemory.empty()
        );

        GraduateQueryInterpretationBudgetResult result = manager.budget(request, "very long prompt content");

        assertFalse(result.requestFits());
        assertEquals("AI_QUERY_INTERPRETATION_BUDGET_EXCEEDED", result.diagnosticCategory());
        assertEquals("What programs does AUB offer?", result.request().userMessage());
        assertEquals(1.0, meterRegistry.find("uniai.ai.request.estimated_tokens")
                .tags("operation", "interpretation", "provider", "gemini", "stage", "original")
                .summary()
                .count());
        assertEquals(1.0, meterRegistry.find("uniai.ai.request.estimated_tokens")
                .tags("operation", "interpretation", "provider", "gemini", "stage", "final")
                .summary()
                .count());
        assertEquals(1.0, meterRegistry.find("uniai.ai.request.estimated_tokens")
                .tags("operation", "interpretation", "provider", "gemini", "stage", "reserved_output")
                .summary()
                .count());
        assertEquals(1.0, meterRegistry.find("uniai.ai.budget.rejections")
                .tags("operation", "interpretation", "provider", "gemini")
                .counter()
                .count());
    }

    private GraduateQueryInterpretationBudgetManager manager(
            long maxInputTokens,
            int maxOutputTokens,
            int historyMessageLimit,
            String provider,
            int requestOverheadTokens
    ) {
        return manager(maxInputTokens, maxOutputTokens, historyMessageLimit, provider, requestOverheadTokens, null);
    }

    private GraduateQueryInterpretationBudgetManager manager(
            long maxInputTokens,
            int maxOutputTokens,
            int historyMessageLimit,
            String provider,
            int requestOverheadTokens,
            SimpleMeterRegistry meterRegistry
    ) {
        GraduateQueryInterpretationBudgetConfiguration configuration = new GraduateQueryInterpretationBudgetConfiguration(
                true,
                maxInputTokens,
                maxOutputTokens,
                historyMessageLimit,
                "prompts/graduate-query-interpreter-prompt.txt"
        );
        AiContextBudgetConfiguration estimatorConfiguration = new AiContextBudgetConfiguration(
                1000,
                100,
                100,
                100,
                4,
                requestOverheadTokens,
                Map.of(provider, new AiContextBudgetConfiguration.ProviderBudget(1000, 100, 100, 100, requestOverheadTokens))
        );
        return new GraduateQueryInterpretationBudgetManager(configuration, new AiTokenEstimator(estimatorConfiguration), provider, meterRegistry);
    }

    private AiConversationMessage msg(String role, String content) {
        return AiConversationMessage.builder().role(role).content(content).build();
    }
}
