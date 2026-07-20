package com.uniai.chat.application.retrieval;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.domain.model.CampusCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateKnowledgeQueryInterpreterTest {

    private GraduateKnowledgeQueryInterpreter interpreter;
    private List<UniversityCatalog> catalogs;

    @BeforeEach
    void setUp() {
        interpreter = new GraduateKnowledgeQueryInterpreter();
        catalogs = List.of(
                UniversityCatalog.builder().id(1L).name("American University of Beirut").nameAr("الجامعة الأميركية في بيروت").acronym("AUB").city("Beirut").build(),
                university(2L, "Université Saint-Joseph", "USJ"),
                university(3L, "Lebanese American University", "LAU"),
                UniversityCatalog.builder().id(4L).name("Lebanese National Conservatory").acronym("LNC").city("Tripoli").build(),
                university(5L, "Al Maaref University", "MU")
        );
    }

    @Test
    void shouldResolveProgramLookupForAubMasterPrograms() {
        GraduateKnowledgeQuery query = interpreter.interpret("What master's programs does AUB offer?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.PROGRAM_LOOKUP, query.intent());
        assertEquals(GraduateProgramDetailLevel.LIST, query.detailLevel());
        assertEquals(List.of("MASTER"), query.degreeTypes());
        assertEquals(1, query.resolvedUniversities().size());
        assertEquals("AUB", query.resolvedUniversities().get(0).acronym());
        assertFalse(query.followUpResolved());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveProgramLookupForAubPhdPrograms() {
        GraduateKnowledgeQuery query = interpreter.interpret("What PhD programs are available at AUB?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.PROGRAM_LOOKUP, query.intent());
        assertEquals(List.of("PHD"), query.degreeTypes());
        assertEquals(1, query.resolvedUniversities().size());
        assertEquals("AUB", query.resolvedUniversities().get(0).acronym());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveProgramLookupForUniversityPrograms() {
        GraduateKnowledgeQuery query = interpreter.interpret("What programs does LAU offer?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.PROGRAM_LOOKUP, query.intent());
        assertEquals(1, query.resolvedUniversities().size());
        assertEquals("LAU", query.resolvedUniversities().get(0).acronym());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveGraduateOverviewForBroadUniversityQuestion() {
        GraduateKnowledgeQuery query = interpreter.interpret("What do you know about MU?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.GRADUATE_OVERVIEW, query.intent());
        assertEquals(1, query.resolvedUniversities().size());
        assertEquals("MU", query.resolvedUniversities().get(0).acronym());
        assertFalse(query.followUpResolved());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveGraduateOverviewFromBothUsingLatestUniversity() {
        List<AiConversationMessage> history = List.of(
                message("user", "What do you know about AUB?"),
                message("assistant", "AUB overview")
        );

        GraduateKnowledgeQuery query = interpreter.interpret("both please", history, catalogs);

        assertEquals(GraduateKnowledgeIntent.GRADUATE_OVERVIEW, query.intent());
        assertTrue(query.followUpResolved());
        assertEquals(1, query.resolvedUniversities().size());
        assertEquals("AUB", query.resolvedUniversities().get(0).acronym());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldClassifyObviousCasualMessagesAsGeneralChat() {
        for (String message : List.of("hi", "hello", "thank you", "how are you?", "what can you do?")) {
            GraduateKnowledgeQuery query = interpreter.interpret(message, List.of(), catalogs);

            assertEquals(GraduateKnowledgeIntent.GENERAL_CHAT, query.intent(), message);
            assertTrue(query.resolvedUniversities().isEmpty(), message);
            assertFalse(query.ambiguous(), message);
        }
    }

    @Test
    void shouldResolveProgramLookupForArabicUniversityName() {
        GraduateKnowledgeQuery query = interpreter.interpret("ما هي برامج الجامعة الأميركية في بيروت؟", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.PROGRAM_LOOKUP, query.intent());
        assertEquals(1, query.resolvedUniversities().size());
        assertEquals("AUB", query.resolvedUniversities().get(0).acronym());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveTuitionAggregationForAverageTuitionAtAub() {
        GraduateKnowledgeQuery query = interpreter.interpret("What is the average tuition at AUB?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.TUITION_AGGREGATION, query.intent());
        assertEquals(1, query.resolvedUniversities().size());
        assertEquals("AUB", query.resolvedUniversities().get(0).acronym());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldNotTreatProgramSubjectAsCityForTuitionQueries() {
        GraduateKnowledgeQuery lauQuery = interpreter.interpret(
                "What is the tuition for the Master's in Computer Science at LAU?", List.of(), catalogs);
        GraduateKnowledgeQuery aubQuery = interpreter.interpret(
                "What is the tuition for Computer Science at AUB?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.TUITION_AGGREGATION, lauQuery.intent());
        assertEquals(GraduateKnowledgeIntent.TUITION_AGGREGATION, aubQuery.intent());
        assertNull(lauQuery.filters().city());
        assertNull(aubQuery.filters().city());
    }

    @Test
    void shouldResolveCampusCountForStructuredCity() {
        GraduateKnowledgeQuery query = interpreter.interpret("How many campuses are in Beirut?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.LOCATION_LOOKUP, query.intent());
        assertEquals(GraduateKnowledgeResource.CAMPUS, query.resource());
        assertEquals(GraduateKnowledgeOperation.COUNT, query.operation());
        assertEquals("Beirut", query.filters().city());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveUniversitiesByCityAsLogicalInstitutions() {
        GraduateKnowledgeQuery query = interpreter.interpret("Which universities are in Beirut?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.LOCATION_LOOKUP, query.intent());
        assertEquals(GraduateKnowledgeResource.UNIVERSITY, query.resource());
        assertEquals(GraduateKnowledgeOperation.LIST, query.operation());
        assertEquals("Beirut", query.filters().city());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveUniversityCountAsLocationLookup() {
        GraduateKnowledgeQuery query = interpreter.interpret("How many universities do we have?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.LOCATION_LOOKUP, query.intent());
        assertEquals(GraduateKnowledgeResource.UNIVERSITY, query.resource());
        assertEquals(GraduateKnowledgeOperation.COUNT, query.operation());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveCampusNameToItsUniversityAndCity() {
        UniversityCatalog aub = UniversityCatalog.builder()
                .id(1L).name("American University of Beirut").acronym("AUB")
                .campuses(List.of(CampusCatalog.builder().name("Marine Research").city("Batroun").build()))
                .build();

        GraduateKnowledgeQuery query = interpreter.interpret("Where is the Marine Research campus?", List.of(), List.of(aub));

        assertEquals(GraduateKnowledgeIntent.LOCATION_LOOKUP, query.intent());
        assertEquals(GraduateKnowledgeResource.CAMPUS, query.resource());
        assertEquals("Batroun", query.filters().city());
        assertEquals("AUB", query.resolvedUniversities().get(0).acronym());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveCampusExistenceForUniversity() {
        GraduateKnowledgeQuery query = interpreter.interpret("Does AUB have a campus?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.LOCATION_LOOKUP, query.intent());
        assertEquals(GraduateKnowledgeResource.CAMPUS, query.resource());
        assertEquals(GraduateKnowledgeOperation.EXISTS, query.operation());
        assertEquals("AUB", query.resolvedUniversities().get(0).acronym());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldKeepLuCampusQuestionScopedAndRejectUnknownUniversityScope() {
        UniversityCatalog lu = UniversityCatalog.builder()
                .id(11L).name("Lebanese University").acronym("UL")
                .campuses(List.of(CampusCatalog.builder().name("Saida Campus").city("Saida").build()))
                .build();
        UniversityCatalog liu = UniversityCatalog.builder()
                .id(27L).name("Lebanese International University").acronym("LIU")
                .campuses(List.of(CampusCatalog.builder().name("Nabatieh Campus").city("Nabatieh").build()))
                .build();
        List<UniversityCatalog> locationCatalogs = List.of(lu, liu);

        GraduateKnowledgeQuery scoped = interpreter.interpret(
                "Does LU have a campus in Nabatieh?", List.of(), locationCatalogs);
        assertEquals(GraduateKnowledgeIntent.LOCATION_LOOKUP, scoped.intent());
        assertEquals(11L, scoped.resolvedUniversities().get(0).id());
        assertEquals("Nabatieh", scoped.filters().city());
        assertFalse(scoped.ambiguous());

        GraduateKnowledgeQuery unknown = interpreter.interpret(
                "Does XYZ University have a campus in Nabatieh?", List.of(), locationCatalogs);
        assertTrue(unknown.ambiguous());
        assertTrue(unknown.resolvedUniversities().isEmpty());
        assertEquals("Nabatieh", unknown.filters().city());

        GraduateKnowledgeQuery broad = interpreter.interpret(
                "Which universities have campuses in Nabatieh?", List.of(), locationCatalogs);
        assertFalse(broad.ambiguous());
        assertTrue(broad.resolvedUniversities().isEmpty());
        assertEquals("Nabatieh", broad.filters().city());
    }

    @Test
    void shouldResolveAcademicProgramCountWithTypedRouting() {
        GraduateKnowledgeQuery query = interpreter.interpret("How many master's programs does AUB offer?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP, query.intent());
        assertEquals(GraduateKnowledgeResource.PROGRAM, query.resource());
        assertEquals(GraduateKnowledgeOperation.COUNT, query.operation());
        assertEquals(List.of("MASTER"), query.degreeTypes());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveFacultyListWithoutFuzzyAcademicMatching() {
        GraduateKnowledgeQuery query = interpreter.interpret("What faculties does AUB have?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP, query.intent());
        assertEquals(GraduateKnowledgeResource.FACULTY, query.resource());
        assertEquals(GraduateKnowledgeOperation.LIST, query.operation());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveExactDepartmentExistenceRequest() {
        GraduateKnowledgeQuery query = interpreter.interpret("Does AUB have a department of Computer Science?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP, query.intent());
        assertEquals(GraduateKnowledgeResource.DEPARTMENT, query.resource());
        assertEquals(GraduateKnowledgeOperation.EXISTS, query.operation());
        assertEquals("computer science", query.filters().departmentName());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveTuitionAggregationForAubAndUsjComparison() {
        GraduateKnowledgeQuery query = interpreter.interpret("Compare tuition at AUB and USJ.", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.TUITION_AGGREGATION, query.intent());
        assertEquals(2, query.resolvedUniversities().size());
        assertEquals(List.of("AUB", "USJ"), query.resolvedUniversities().stream().map(ResolvedUniversity::acronym).toList());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldUseTypedCompareOperationForUniversityComparison() {
        GraduateKnowledgeQuery query = interpreter.interpret("Compare AUB and LAU.", List.of(), catalogs);

        assertEquals(GraduateKnowledgeOperation.COMPARE, query.operation());
        assertEquals(GraduateKnowledgeComparisonDimension.UNIVERSITY, query.followUpContext().comparisonDimension());
        assertEquals(List.of("AUB", "LAU"), query.resolvedUniversities().stream().map(ResolvedUniversity::acronym).toList());
    }

    @Test
    void shouldKeepSubjectiveBestQuestionAmbiguous() {
        GraduateKnowledgeQuery query = interpreter.interpret("Which is the best university?", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS, query.intent());
        assertTrue(query.ambiguous());
    }

    @Test
    void shouldComposeProgramFiltersWithCityLanguageAdmissionAndTopic() {
        GraduateKnowledgeQuery query = interpreter.interpret(
                "Show English master's computer science programs in Beirut requiring GMAT at AUB",
                List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.PROGRAM_LOOKUP, query.intent());
        assertEquals("Beirut", query.filters().city());
        assertEquals(List.of("computer science"), query.filters().topicKeywords());
        assertEquals(List.of("english"), query.filters().languages());
        assertEquals(List.of("GMAT"), query.filters().admissionRequirementTypes());
        assertEquals(List.of("AUB"), query.resolvedUniversities().stream().map(ResolvedUniversity::acronym).toList());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldAllowCityAsBoundedProgramScopeWithoutUniversity() {
        GraduateKnowledgeQuery query = interpreter.interpret(
                "Find master's programs in Tripoli",
                List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.PROGRAM_LOOKUP, query.intent());
        assertEquals("Tripoli", query.filters().city());
        assertTrue(query.resolvedUniversities().isEmpty());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveFollowUpSameAtLauUsingLatestTuitionIntent() {
        List<AiConversationMessage> history = List.of(
                message("user", "What is the average tuition at AUB?"),
                message("assistant", "AUB tuition answer")
        );

        GraduateKnowledgeQuery query = interpreter.interpret("Same at LAU?", history, catalogs);

        assertEquals(GraduateKnowledgeIntent.TUITION_AGGREGATION, query.intent());
        assertTrue(query.followUpResolved());
        assertEquals(1, query.resolvedUniversities().size());
        assertEquals("LAU", query.resolvedUniversities().get(0).acronym());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldIgnoreAssistantTurnsWhenInferringFollowUpSignals() {
        List<AiConversationMessage> history = List.of(
                message("user", "What master's programs does AUB offer?"),
                message("assistant", "LAU also offers several PhD programs."),
                message("user", "AUB programs answer")
        );

        GraduateKnowledgeQuery query = interpreter.interpret("How much does it cost?", history, catalogs);

        assertEquals(GraduateKnowledgeIntent.TUITION_AGGREGATION, query.intent());
        assertTrue(query.followUpResolved());
        assertEquals(1, query.resolvedUniversities().size());
        assertEquals("AUB", query.resolvedUniversities().get(0).acronym());
        assertEquals(List.of("MASTER"), query.degreeTypes());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveCompareItWithUsjUsingLatestProgramIntent() {
        List<AiConversationMessage> history = List.of(
                message("user", "What master's programs does AUB offer?"),
                message("assistant", "AUB master's answer")
        );

        GraduateKnowledgeQuery query = interpreter.interpret("Compare it with USJ.", history, catalogs);

        assertEquals(GraduateKnowledgeIntent.PROGRAM_LOOKUP, query.intent());
        assertTrue(query.followUpResolved());
        assertEquals(List.of("AUB", "USJ"), query.resolvedUniversities().stream().map(ResolvedUniversity::acronym).toList());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldResolveFollowUpAndForPhdUsingLatestProgramIntent() {
        List<AiConversationMessage> history = List.of(
                message("user", "What master's programs does AUB offer?"),
                message("assistant", "AUB master's answer")
        );

        GraduateKnowledgeQuery query = interpreter.interpret("And for PhD?", history, catalogs);

        assertEquals(GraduateKnowledgeIntent.PROGRAM_LOOKUP, query.intent());
        assertTrue(query.followUpResolved());
        assertEquals(List.of("PHD"), query.degreeTypes());
        assertEquals(1, query.resolvedUniversities().size());
        assertEquals("AUB", query.resolvedUniversities().get(0).acronym());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldPreserveGraduateOverviewWhenSameQuestionReplacesOnlyUniversity() {
        List<AiConversationMessage> history = List.of(
                message("user", "What do you know about MU?"),
                message("assistant", "MU overview")
        );

        GraduateKnowledgeQuery query = interpreter.interpret("same question for AUB?", history, catalogs);

        assertEquals(GraduateKnowledgeIntent.GRADUATE_OVERVIEW, query.intent());
        assertTrue(query.followUpResolved());
        assertEquals(1, query.resolvedUniversities().size());
        assertEquals("AUB", query.resolvedUniversities().get(0).acronym());
        assertFalse(query.ambiguous());
    }

    @Test
    void shouldMarkAmbiguousWhenFollowUpCouldReferToMultipleUniversities() {
        List<AiConversationMessage> history = List.of(
                message("user", "What master's programs does AUB offer?"),
                message("assistant", "AUB answer"),
                message("user", "What master's programs does USJ offer?"),
                message("assistant", "USJ answer")
        );

        GraduateKnowledgeQuery query = interpreter.interpret("What about tuition?", history, catalogs);

        assertEquals(GraduateKnowledgeIntent.TUITION_AGGREGATION, query.intent());
        assertTrue(query.ambiguous());
    }

    @Test
    void shouldReturnUnknownForBlankOrUnsupportedQueries() {
        GraduateKnowledgeQuery blank = interpreter.interpret("   ", List.of(), catalogs);
        GraduateKnowledgeQuery unsupported = interpreter.interpret("Tell me a joke.", List.of(), catalogs);

        assertEquals(GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS, blank.intent());
        assertEquals(GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS, unsupported.intent());
        assertTrue(blank.ambiguous());
        assertTrue(unsupported.ambiguous());
    }

    @Test
    void shouldClassifyHighConfidenceRequestsForProviderBypass() {
        GraduateKnowledgeQuery globalCount = interpreter.interpret("How many universities do we have?", List.of(), catalogs);
        GraduateKnowledgeQuery campusList = interpreter.interpret("List AUB campuses.", List.of(), catalogs);
        GraduateKnowledgeQuery comparison = interpreter.interpret("Compare AUB and USJ campuses.", List.of(), catalogs);

        assertTrue(interpreter.isHighConfidenceDeterministic("How many universities do we have?", globalCount));
        assertTrue(interpreter.isHighConfidenceDeterministic("List AUB campuses.", campusList));
        assertFalse(interpreter.isHighConfidenceDeterministic("Compare AUB and USJ campuses.", comparison));
    }

    private AiConversationMessage message(String role, String content) {
        return AiConversationMessage.builder().role(role).content(content).build();
    }

    private UniversityCatalog university(Long id, String name, String acronym) {
        return UniversityCatalog.builder().id(id).name(name).acronym(acronym).build();
    }
}
