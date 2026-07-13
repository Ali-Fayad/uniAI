package com.uniai.chat.application.budget;

import com.uniai.chat.application.interpretation.GraduateQueryInterpretationResult;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.ConversationMemoryUpdateRequest;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversationMemoryBudgetTest {

    @Test
    void budgetShouldCountMemoryAndPreserveRequiredFields() {
        ConversationMemory memory = new ConversationMemory(
                ConversationMemory.SCHEMA_VERSION,
                List.of(new com.uniai.chat.application.memory.MemoryUniversityRef(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                "PROGRAM_LOOKUP",
                false,
                List.of(),
                List.of("tuition"),
                List.of(),
                List.of(),
                new com.uniai.chat.application.memory.ConversationPreferences("ENGLISH", null, null)
        );
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                false,
                false
        );
        GraduateQueryInterpretationResult interpretationResult = GraduateQueryInterpretationResult.valid(query, 1, 1);
        ConversationMemoryUpdateRequest request = new ConversationMemoryUpdateRequest(
                memory,
                "What master's programs does AUB offer?",
                "Here is the official answer.",
                interpretationResult
        );

        AiTokenEstimator estimator = new AiTokenEstimator(new AiContextBudgetConfiguration(
                2000,
                200,
                1000,
                1000,
                4,
                128,
                java.util.Map.of("gemini", new AiContextBudgetConfiguration.ProviderBudget(2000, 200, 1000, 1000, 128))
        ));
        ConversationMemoryBudgetManager budgetManager = new ConversationMemoryBudgetManager(
                new ConversationMemoryBudgetConfiguration(true, 2000, 200, "prompts/conversation-memory-updater-prompt.txt"),
                estimator,
                "gemini"
        );

        ConversationMemoryBudgetResult withMemory = budgetManager.budget(request, "Memory update prompt");
        ConversationMemoryBudgetResult withoutMemory = budgetManager.budget(
                new ConversationMemoryUpdateRequest(
                        ConversationMemory.empty(),
                        "What master's programs does AUB offer?",
                        "Here is the official answer.",
                        interpretationResult
                ),
                "Memory update prompt"
        );

        assertTrue(withMemory.requestFits());
        assertTrue(withMemory.originalEstimatedInputTokens() > withoutMemory.originalEstimatedInputTokens());
        assertTrue(withMemory.request().previousMemory().equals(memory));
        assertFalse(withMemory.diagnosticCategory().isBlank() && !withMemory.requestFits());
    }

    @Test
    void budgetShouldRejectImpossibleRequests() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        ConversationMemoryBudgetManager budgetManager = new ConversationMemoryBudgetManager(
                new ConversationMemoryBudgetConfiguration(true, 5, 2, "prompts/conversation-memory-updater-prompt.txt"),
                new AiTokenEstimator(new AiContextBudgetConfiguration(
                        2000,
                        200,
                        1000,
                        1000,
                        4,
                        128,
                        java.util.Map.of("gemini", new AiContextBudgetConfiguration.ProviderBudget(2000, 200, 1000, 1000, 128))
                )),
                "gemini",
                meterRegistry
        );

        ConversationMemoryUpdateRequest request = new ConversationMemoryUpdateRequest(
                ConversationMemory.empty(),
                "This cannot possibly fit",
                "assistant",
                null
        );

        ConversationMemoryBudgetResult result = budgetManager.budget(request, "prompt");
        assertFalse(result.requestFits());
        assertEquals(1.0, meterRegistry.find("uniai.ai.budget.rejections")
                .tags("operation", "memory_update", "provider", "gemini")
                .counter()
                .count());
    }
}
