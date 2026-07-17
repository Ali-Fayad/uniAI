package com.uniai.chat.application.retrieval;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.ConversationPreferences;
import com.uniai.chat.application.memory.MemoryUniversityRef;
import com.uniai.catalog.domain.model.UniversityCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateFollowUpResolverTest {

    private GraduateFollowUpResolver resolver;
    private List<UniversityCatalog> catalogs;

    @BeforeEach
    void setUp() {
        resolver = new GraduateFollowUpResolver();
        catalogs = List.of(
                university(1L, "American University of Beirut", "AUB"),
                university(2L, "Université Saint-Joseph", "USJ"),
                university(3L, "Lebanese American University", "LAU")
        );
    }

    @Test
    void shouldInheritUniqueUniversityFromRecentHistoryForCostFollowUp() {
        ConversationMemory memory = ConversationMemory.empty();
        List<AiConversationMessage> history = List.of(
                message("user", "What master's programs does AUB offer?"),
                message("assistant", "AUB programs answer")
        );
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.TUITION_AGGREGATION,
                List.of(),
                List.of("MASTER"),
                null,
                false,
                false
        );

        GraduateFollowUpResolutionResult result = resolver.resolve("How much does it cost?", candidate, history, memory, catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.RESOLVED, result.status());
        assertEquals(GraduateKnowledgeIntent.TUITION_AGGREGATION, result.resolvedQuery().intent());
        assertEquals("AUB", result.resolvedQuery().resolvedUniversities().get(0).acronym());
    }

    @Test
    void shouldLeaveGeneralChatOutsideGraduateFollowUpResolution() {
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.GENERAL_CHAT,
                List.of(),
                List.of(),
                null,
                false,
                false
        );

        GraduateFollowUpResolutionResult result = resolver.resolve("tell me a joke", candidate, List.of(), ConversationMemory.empty(), catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.UNCHANGED, result.status());
        assertEquals(GraduateKnowledgeIntent.GENERAL_CHAT, result.resolvedQuery().intent());
    }

    @Test
    void shouldIgnoreAssistantTurnsWhenInferringFollowUpSignals() {
        ConversationMemory memory = ConversationMemory.empty();
        List<AiConversationMessage> history = List.of(
                message("user", "What master's programs does AUB offer?"),
                message("assistant", "LAU also offers several PhD programs."),
                message("user", "AUB programs answer")
        );
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.TUITION_AGGREGATION,
                List.of(),
                List.of(),
                null,
                false,
                false
        );

        GraduateFollowUpResolutionResult result = resolver.resolve("How much does it cost?", candidate, history, memory, catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.RESOLVED, result.status());
        assertEquals(GraduateKnowledgeIntent.TUITION_AGGREGATION, result.resolvedQuery().intent());
        assertEquals("AUB", result.resolvedQuery().resolvedUniversities().get(0).acronym());
        assertEquals(List.of("MASTER"), result.resolvedQuery().degreeTypes());
    }

    @Test
    void shouldLetExplicitUniversityOverrideInheritedState() {
        ConversationMemory memory = new ConversationMemory(
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
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                false,
                false
        );

        GraduateFollowUpResolutionResult result = resolver.resolve("What about LAU?", candidate, List.of(), memory, catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.RESOLVED, result.status());
        assertEquals(List.of("LAU"), result.resolvedQuery().resolvedUniversities().stream().map(ResolvedUniversity::acronym).toList());
        assertEquals(List.of("MASTER"), result.resolvedQuery().degreeTypes());
    }

    @Test
    void shouldPreserveGraduateOverviewWhenSameQuestionReplacesOnlyUniversity() {
        ConversationMemory memory = new ConversationMemory(
                ConversationMemory.SCHEMA_VERSION,
                List.of(new MemoryUniversityRef(5L, "Al Maaref University", "MU")),
                List.of(),
                "GRADUATE_OVERVIEW",
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new ConversationPreferences("ENGLISH", null, null)
        );
        List<AiConversationMessage> history = List.of(
                message("user", "What do you know about MU?"),
                message("assistant", "MU overview")
        );
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS,
                List.of(),
                List.of(),
                null,
                false,
                false
        );

        GraduateFollowUpResolutionResult result = resolver.resolve("same question for AUB?", candidate, history, memory, catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.RESOLVED, result.status());
        assertEquals(GraduateKnowledgeIntent.GRADUATE_OVERVIEW, result.resolvedQuery().intent());
        assertEquals(List.of("AUB"), result.resolvedQuery().resolvedUniversities().stream().map(ResolvedUniversity::acronym).toList());
        assertFalse(result.resolvedQuery().ambiguous());
    }

    @Test
    void shouldResolveComparisonOrdinalsFromActiveComparisonState() {
        ConversationMemory memory = new ConversationMemory(
                ConversationMemory.SCHEMA_VERSION,
                List.of(
                        new MemoryUniversityRef(1L, "American University of Beirut", "AUB"),
                        new MemoryUniversityRef(2L, "Université Saint-Joseph", "USJ")
                ),
                List.of("MASTER"),
                "PROGRAM_LOOKUP",
                true,
                List.of(
                        new MemoryUniversityRef(1L, "American University of Beirut", "AUB"),
                        new MemoryUniversityRef(2L, "Université Saint-Joseph", "USJ")
                ),
                List.of(),
                List.of(),
                List.of(),
                new ConversationPreferences("ENGLISH", null, null)
        );
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                true,
                false
        );

        GraduateFollowUpResolutionResult first = resolver.resolve("The first one.", candidate, List.of(), memory, catalogs);
        GraduateFollowUpResolutionResult second = resolver.resolve("The second university.", candidate, List.of(), memory, catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.RESOLVED, first.status());
        assertEquals("AUB", first.resolvedQuery().resolvedUniversities().get(0).acronym());
        assertEquals(GraduateFollowUpResolutionStatus.RESOLVED, second.status());
        assertEquals("USJ", second.resolvedQuery().resolvedUniversities().get(0).acronym());
    }

    @Test
    void shouldClarifySingularPronounWhenComparisonHasTwoCandidates() {
        ConversationMemory memory = new ConversationMemory(
                ConversationMemory.SCHEMA_VERSION,
                List.of(
                        new MemoryUniversityRef(1L, "American University of Beirut", "AUB"),
                        new MemoryUniversityRef(2L, "Université Saint-Joseph", "USJ")
                ),
                List.of("MASTER"),
                "PROGRAM_LOOKUP",
                true,
                List.of(
                        new MemoryUniversityRef(1L, "American University of Beirut", "AUB"),
                        new MemoryUniversityRef(2L, "Université Saint-Joseph", "USJ")
                ),
                List.of(),
                List.of(),
                List.of(),
                new ConversationPreferences("ENGLISH", null, null)
        );
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                true,
                false
        );

        GraduateFollowUpResolutionResult result = resolver.resolve("it", candidate, List.of(), memory, catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.CLARIFICATION_REQUIRED, result.status());
        assertTrue(result.clarificationReason().contains("COMPARISON"));
    }

    @Test
    void shouldReplaceInheritedDegreeWithExplicitPhd() {
        ConversationMemory memory = new ConversationMemory(
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
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                false,
                false
        );

        GraduateFollowUpResolutionResult result = resolver.resolve("And for PhD?", candidate, List.of(), memory, catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.RESOLVED, result.status());
        assertEquals(List.of("PHD"), result.resolvedQuery().degreeTypes());
        assertEquals("AUB", result.resolvedQuery().resolvedUniversities().get(0).acronym());
    }

    @Test
    void shouldClarifyWhenExplicitUniversityCannotBeResolved() {
        ConversationMemory memory = new ConversationMemory(
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
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                false,
                false
        );

        GraduateFollowUpResolutionResult result = resolver.resolve("What about XYZ University?", candidate, List.of(), memory, catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.CLARIFICATION_REQUIRED, result.status());
        assertTrue(result.clarificationReason().contains("UNIVERSITY"));
        assertTrue(result.resolvedQuery().resolvedUniversities().isEmpty());
        assertEquals(List.of("MASTER"), result.resolvedQuery().degreeTypes());
    }

    @Test
    void shouldClarifyWhenUniversityCorrectionMentionsUnknownUniversity() {
        ConversationMemory memory = new ConversationMemory(
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
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                false,
                false
        );

        GraduateFollowUpResolutionResult result = resolver.resolve("No, I meant XYZ University", candidate, List.of(), memory, catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.CLARIFICATION_REQUIRED, result.status());
        assertTrue(result.clarificationReason().contains("UNIVERSITY"));
        assertTrue(result.resolvedQuery().resolvedUniversities().isEmpty());
    }

    @Test
    void shouldReturnUnsupportedWhenCurrentDegreeIsExplicitlyUnsupported() {
        ConversationMemory memory = new ConversationMemory(
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
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                false,
                false
        );

        GraduateFollowUpResolutionResult result = resolver.resolve("Actually, bachelor", candidate, List.of(), memory, catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.UNSUPPORTED, result.status());
        assertNull(result.resolvedQuery());
    }

    @Test
    void shouldClarifyWhenCurrentDegreeReferenceCannotBeResolved() {
        ConversationMemory memory = new ConversationMemory(
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
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                false,
                false
        );

        GraduateFollowUpResolutionResult result = resolver.resolve("What about the graduate diploma?", candidate, List.of(), memory, catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.CLARIFICATION_REQUIRED, result.status());
        assertTrue(result.clarificationReason().contains("DEGREE"));
        assertTrue(result.resolvedQuery().degreeTypes().isEmpty());
        assertEquals("AUB", result.resolvedQuery().resolvedUniversities().get(0).acronym());
    }

    @Test
    void shouldClarifyWhenHistoryAndMemoryConflictWithoutExplicitDisambiguation() {
        ConversationMemory memory = new ConversationMemory(
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
        List<AiConversationMessage> history = List.of(
                message("user", "What master's programs does USJ offer?"),
                message("assistant", "USJ answer")
        );
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.TUITION_AGGREGATION,
                List.of(),
                List.of("MASTER"),
                null,
                false,
                false
        );

        GraduateFollowUpResolutionResult result = resolver.resolve("How much does it cost?", candidate, history, memory, catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.CLARIFICATION_REQUIRED, result.status());
        assertTrue(result.clarificationReason().contains("UNIVERSITY"));
    }

    @Test
    void shouldUseTokenBoundariesForStandaloneFollowUpWords() {
        assertFalse(GraduateKnowledgeResolutionSupport.isFollowUpMessage("What is the tuition?"));
        assertFalse(GraduateKnowledgeResolutionSupport.isFollowUpMessage("How many credits?"));
        assertFalse(GraduateKnowledgeResolutionSupport.isFollowUpMessage("Admission"));
        assertFalse(GraduateKnowledgeResolutionSupport.isFollowUpMessage("Which universities are supported?"));
        assertFalse(GraduateKnowledgeResolutionSupport.isFollowUpMessage("Check eligibility requirements."));
        assertTrue(GraduateKnowledgeResolutionSupport.isFollowUpMessage("How much does it cost?"));
        assertTrue(GraduateKnowledgeResolutionSupport.isFollowUpMessage("Is it cheaper?"));
        assertTrue(GraduateKnowledgeResolutionSupport.isFollowUpMessage("What about there?"));
        assertTrue(GraduateKnowledgeResolutionSupport.isFollowUpMessage("The same degree"));
    }

    @Test
    void shouldPreserveCandidateQueryWhenNoResolutionIsNeeded() {
        GraduateKnowledgeQuery candidate = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                false,
                false
        );

        GraduateFollowUpResolutionResult result = resolver.resolve("What master's programs does AUB offer?", candidate, List.of(), ConversationMemory.empty(), catalogs);

        assertEquals(GraduateFollowUpResolutionStatus.UNCHANGED, result.status());
        assertEquals(candidate, result.resolvedQuery());
        assertFalse(result.resolutionSources().isEmpty());
    }

    private AiConversationMessage message(String role, String content) {
        return AiConversationMessage.builder().role(role).content(content).build();
    }

    private UniversityCatalog university(Long id, String name, String acronym) {
        return UniversityCatalog.builder().id(id).name(name).acronym(acronym).build();
    }
}
