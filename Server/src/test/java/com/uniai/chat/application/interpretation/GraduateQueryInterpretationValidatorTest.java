package com.uniai.chat.application.interpretation;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateQueryInterpretationValidatorTest {

    private GraduateQueryInterpretationValidator validator;
    private List<UniversityCatalog> catalogs;

    @BeforeEach
    void setUp() {
        validator = new GraduateQueryInterpretationValidator();
        catalogs = List.of(
                university(1L, "American University of Beirut", "AUB", "الجامعة الأميركية في بيروت"),
                university(2L, "Université Saint-Joseph", "USJ", "الجامعة القديس يوسف")
        );
    }

    @Test
    void shouldValidateAndNormalizeProgramLookup() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                " program_lookup ",
                List.of("AUB"),
                List.of(" master "),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.VALID, result.status());
        assertEquals(1, result.resolvedUniversityCount());
        assertEquals(1, result.degreeTypeCount());
        assertEquals("AUB", result.query().resolvedUniversities().get(0).acronym());
    }

    @Test
    void shouldResolveArabicUniversityNames() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of("الجامعة الأميركية في بيروت"),
                List.of("MASTER"),
                "DETAILS",
                null,
                null,
                List.of(),
                false,
                null,
                List.of()
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.VALID, result.status());
        assertEquals("AUB", result.query().resolvedUniversities().get(0).acronym());
    }

    @Test
    void shouldValidateGraduateOverviewForBroadUniversityQuestion() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "GRADUATE_OVERVIEW",
                List.of("AUB"),
                List.of(),
                null,
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.VALID, result.status());
        assertEquals(GraduateKnowledgeIntent.GRADUATE_OVERVIEW, result.query().intent());
        assertEquals(1, result.resolvedUniversityCount());
    }

    @Test
    void shouldValidateTypedProgramRoutingMetadata() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of("AUB"),
                List.of("MASTER"),
                "LIST",
                false,
                false,
                List.of("computer science"),
                false,
                null,
                List.of(),
                "PROGRAM",
                "LIST"
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.VALID, result.status());
        assertEquals("PROGRAM", result.query().resource().name());
        assertEquals("LIST", result.query().operation().name());
        assertEquals(List.of("computer science"), result.query().topicKeywords());
    }

    @Test
    void shouldValidateTypedAcademicStructureRouting() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "ACADEMIC_STRUCTURE_LOOKUP",
                List.of("AUB"),
                List.of("MASTER"),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of(),
                "DEPARTMENT",
                "EXISTS",
                null,
                null,
                "Computer Science",
                List.of(),
                List.of(),
                null
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.VALID, result.status());
        assertEquals(GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP, result.query().intent());
        assertEquals("DEPARTMENT", result.query().resource().name());
        assertEquals("EXISTS", result.query().operation().name());
        assertEquals("Computer Science", result.query().filters().departmentName());
    }

    @Test
    void shouldValidateLocationLookupWithoutUniversityWhenCityIsProvided() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "LOCATION_LOOKUP",
                List.of(),
                List.of(),
                null,
                false,
                false,
                List.of(),
                false,
                null,
                List.of(),
                "UNIVERSITY",
                "COUNT",
                "Beirut"
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.VALID, result.status());
        assertEquals(GraduateKnowledgeIntent.LOCATION_LOOKUP, result.query().intent());
        assertEquals("Beirut", result.query().filters().city());
        assertEquals(GraduateKnowledgeOperation.COUNT, result.query().operation());
    }

    @Test
    void shouldRejectTypedMetadataThatConflictsWithIntent() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of("AUB"),
                List.of("MASTER"),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of(),
                "UNIVERSITY",
                "COUNT"
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.INVALID, result.status());
        assertEquals("AI_QUERY_INTERPRETATION_RESOURCE_OPERATION_UNSUPPORTED", result.failureCategory());
    }

    @Test
    void shouldValidateGeneralChatWithoutGraduateEntities() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "GENERAL_CHAT",
                List.of(),
                List.of(),
                null,
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.VALID, result.status());
        assertEquals(GraduateKnowledgeIntent.GENERAL_CHAT, result.query().intent());
        assertTrue(result.query().resolvedUniversities().isEmpty());
        assertFalse(result.query().ambiguous());
    }

    @Test
    void shouldMarkUnsupportedForBachelorRequests() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of("AUB"),
                List.of("BACHELOR"),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.UNSUPPORTED, result.status());
        assertNull(result.query());
        assertTrue(result.unsupportedConstraints().contains("BACHELOR"));
    }

    @Test
    void shouldMarkAmbiguousForInventedUniversityNames() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of("Imaginary University"),
                List.of("MASTER"),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.AMBIGUOUS, result.status());
        assertNotNull(result.query());
        assertTrue(result.query().resolvedUniversities().isEmpty());
        assertEquals(List.of("MASTER"), result.query().degreeTypes());
    }

    @Test
    void shouldDeduplicateUniversityMentions() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1,
                "PROGRAM_LOOKUP",
                List.of("AUB", "American University of Beirut"),
                List.of("MASTER"),
                "LIST",
                false,
                false,
                List.of(),
                false,
                null,
                List.of()
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.VALID, result.status());
        assertEquals(1, result.query().resolvedUniversities().size());
    }

    @Test
    void shouldValidateCityScopedMultiFilterQueryWithoutUniversity() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1, "PROGRAM_LOOKUP", List.of(), List.of("MASTER"), "LIST", false, false,
                List.of("computer science"), false, null, List.of(), "PROGRAM", "LIST",
                "Beirut", null, null, List.of("English"), List.of("GMAT"), null,
                null, null, null, null, null, null, null, "NAME", "ASC", 5
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.VALID, result.status());
        assertTrue(result.query().resolvedUniversities().isEmpty());
        assertEquals("Beirut", result.query().filters().city());
        assertEquals(5, result.query().limit());
    }

    @Test
    void shouldRejectRangeThresholdCombination() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1, "TUITION_AGGREGATION", List.of("AUB"), List.of("MASTER"), null, false, false,
                List.of(), false, null, List.of(), "PROGRAM", "AGGREGATE",
                null, null, null, List.of(), List.of(), null,
                "RANGE", "LT", "10000", "USD", "PER_YEAR", null, "PROGRAM", "TUITION", "ASC", 5
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.INVALID, result.status());
        assertEquals("AI_QUERY_INTERPRETATION_RESOURCE_OPERATION_UNSUPPORTED", result.failureCategory());
    }

    @Test
    void shouldRequireObjectiveDimensionForCompare() {
        GraduateQueryInterpretation interpretation = new GraduateQueryInterpretation(
                1, "PROGRAM_LOOKUP", List.of("AUB", "USJ"), List.of(), "LIST", false, true,
                List.of(), false, null, List.of(), "PROGRAM", "COMPARE", null, null, null,
                List.of(), List.of(), null, null, null, null, null, null, null, null, null, null, null, "PROGRAM_COUNT"
        );

        GraduateQueryInterpretationResult result = validator.validate(interpretation, catalogs);

        assertEquals(GraduateQueryInterpretationStatus.VALID, result.status());
        assertEquals(GraduateKnowledgeOperation.COMPARE, result.query().operation());
        assertEquals("PROGRAM_COUNT", result.query().followUpContext().comparisonDimension().name());
    }

    private UniversityCatalog university(Long id, String name, String acronym, String nameAr) {
        return UniversityCatalog.builder()
                .id(id)
                .name(name)
                .acronym(acronym)
                .nameAr(nameAr)
                .build();
    }
}
