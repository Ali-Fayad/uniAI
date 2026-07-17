package com.uniai.chat.application.memory;

import com.uniai.chat.application.interpretation.GraduateQueryInterpretationResult;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversationMemoryTriggerPolicyTest {

    private final ConversationMemoryTriggerPolicy policy = new ConversationMemoryTriggerPolicy();

    @Test
    void shouldUpdateWhenMeaningfulStateChangesOrSafetyRefreshIsDue() {
        ConversationMemory previous = new ConversationMemory(
                ConversationMemory.SCHEMA_VERSION,
                List.of(new MemoryUniversityRef(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                "PROGRAM_LOOKUP",
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new ConversationPreferences("ENGLISH", null, null)
        );

        GraduateQueryInterpretationResult sameState = GraduateQueryInterpretationResult.valid(
                new GraduateKnowledgeQuery(
                        GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                        List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                        List.of("MASTER"),
                        GraduateProgramDetailLevel.LIST,
                        false,
                        false
                ),
                1,
                1
        );

        assertFalse(policy.shouldUpdate(previous, sameState, 1L, "Hello"));
        assertTrue(policy.shouldUpdate(previous, sameState, 5L, "Hello"));

        GraduateQueryInterpretationResult intentChanged = GraduateQueryInterpretationResult.valid(
                new GraduateKnowledgeQuery(
                        GraduateKnowledgeIntent.TUITION_AGGREGATION,
                        List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                        List.of("MASTER"),
                        GraduateProgramDetailLevel.LIST,
                        false,
                        false
                ),
                1,
                1
        );
        assertTrue(policy.shouldUpdate(previous, intentChanged, 1L, "Hello"));

        GraduateQueryInterpretationResult universityChanged = GraduateQueryInterpretationResult.valid(
                new GraduateKnowledgeQuery(
                        GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                        List.of(new ResolvedUniversity(2L, "Université Saint-Joseph", "USJ")),
                        List.of("MASTER"),
                        GraduateProgramDetailLevel.LIST,
                        false,
                        false
                ),
                1,
                1
        );
        assertTrue(policy.shouldUpdate(previous, universityChanged, 1L, "Hello"));

        GraduateQueryInterpretationResult degreeChanged = GraduateQueryInterpretationResult.valid(
                new GraduateKnowledgeQuery(
                        GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                        List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                        List.of("PHD"),
                        GraduateProgramDetailLevel.LIST,
                        false,
                        false
                ),
                1,
                1
        );
        assertTrue(policy.shouldUpdate(previous, degreeChanged, 1L, "Hello"));

        GraduateQueryInterpretationResult comparisonChanged = GraduateQueryInterpretationResult.valid(
                new GraduateKnowledgeQuery(
                        GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                        List.of(
                                new ResolvedUniversity(1L, "American University of Beirut", "AUB"),
                                new ResolvedUniversity(2L, "Université Saint-Joseph", "USJ")
                        ),
                        List.of("MASTER"),
                        GraduateProgramDetailLevel.LIST,
                        true,
                        false
                ),
                2,
                1
        );
        assertTrue(policy.shouldUpdate(previous, comparisonChanged, 1L, "Hello"));
        assertTrue(policy.shouldUpdate(previous, sameState, 1L, "I prefer online programs"));
    }

    @Test
    void shouldNotReplaceGraduateMemoryForGeneralChat() {
        GraduateQueryInterpretationResult generalChat = GraduateQueryInterpretationResult.valid(
                new GraduateKnowledgeQuery(
                        GraduateKnowledgeIntent.GENERAL_CHAT,
                        List.of(),
                        List.of(),
                        null,
                        false,
                        false
                ),
                0,
                0
        );

        assertFalse(policy.shouldUpdate(ConversationMemory.empty(), generalChat, 5L, "Thanks!"));
    }
}
