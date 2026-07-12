package com.uniai.chat.application.memory;

import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversationMemoryMergePolicyTest {

    private final ConversationMemoryMergePolicy mergePolicy = new ConversationMemoryMergePolicy();

    @Test
    void mergeShouldSeedStateFromValidatedQueryWhenMemoryIsEmpty() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(
                        new ResolvedUniversity(1L, "American University of Beirut", "AUB"),
                        new ResolvedUniversity(2L, "Université Saint-Joseph", "USJ")
                ),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                true,
                false
        );

        ConversationMemory merged = mergePolicy.merge(ConversationMemory.empty(), emptyPatch(), query);

        assertEquals("PROGRAM_LOOKUP", merged.lastIntent());
        assertTrue(merged.comparisonActive());
        assertEquals(List.of("MASTER"), merged.activeDegreeTypes());
        assertEquals(List.of(
                new MemoryUniversityRef(1L, "American University of Beirut", "AUB"),
                new MemoryUniversityRef(2L, "Université Saint-Joseph", "USJ")
        ), merged.activeUniversities());
        assertEquals(List.of(
                new MemoryUniversityRef(1L, "American University of Beirut", "AUB"),
                new MemoryUniversityRef(2L, "Université Saint-Joseph", "USJ")
        ), merged.comparisonUniversities());
    }

    @Test
    void mergeShouldHonorExplicitClearAndDeterministicOrdering() {
        ConversationMemory previous = new ConversationMemory(
                ConversationMemory.SCHEMA_VERSION,
                List.of(
                        new MemoryUniversityRef(9L, "Old University", "OLD"),
                        new MemoryUniversityRef(1L, "American University of Beirut", "AUB")
                ),
                List.of("MASTER"),
                "TUITION_AGGREGATION",
                true,
                List.of(new MemoryUniversityRef(1L, "American University of Beirut", "AUB")),
                List.of("tuition"),
                List.of("remember this"),
                List.of("reference"),
                new ConversationPreferences("English", "BALANCED", "ONLINE")
        );

        ConversationMemoryPatch patch = new ConversationMemoryPatch(
                ConversationMemory.SCHEMA_VERSION,
                null,
                null,
                List.of(),
                List.of("USJ"),
                List.of("OLD"),
                List.of(),
                List.of("PHD"),
                List.of(),
                List.of(),
                List.of("new topic"),
                List.of("tuition"),
                List.of("new correction"),
                List.of("remember this"),
                null,
                List.of("userPreferences", "unresolvedReferences")
        );

        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(
                        new ResolvedUniversity(1L, "American University of Beirut", "AUB"),
                        new ResolvedUniversity(2L, "Université Saint-Joseph", "USJ")
                ),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                true,
                false
        );

        ConversationMemory merged = mergePolicy.merge(previous, patch, query);

        assertEquals("PROGRAM_LOOKUP", merged.lastIntent());
        assertFalse(merged.activeUniversities().stream().anyMatch(ref -> ref != null && Long.valueOf(9L).equals(ref.id())));
        assertEquals(List.of("MASTER", "PHD"), merged.activeDegreeTypes());
        assertEquals(List.of(
                new MemoryUniversityRef(1L, "American University of Beirut", "AUB"),
                new MemoryUniversityRef(2L, "Université Saint-Joseph", "USJ")
        ), merged.activeUniversities());
        assertEquals(List.of(
                new MemoryUniversityRef(1L, "American University of Beirut", "AUB"),
                new MemoryUniversityRef(2L, "Université Saint-Joseph", "USJ")
        ), merged.comparisonUniversities());
        assertEquals(List.of("new topic"), merged.pendingTopics());
        assertEquals(List.of("new correction"), merged.corrections());
        assertTrue(merged.unresolvedReferences().isEmpty());
        assertTrue(merged.userPreferences().isEmpty());
    }

    private ConversationMemoryPatch emptyPatch() {
        return new ConversationMemoryPatch(
                ConversationMemory.SCHEMA_VERSION,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of()
        );
    }
}
