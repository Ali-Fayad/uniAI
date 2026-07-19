package com.uniai.chat.application.retrieval;

import com.uniai.chat.application.interpretation.CanonicalGraduateQueryDraft;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraduateKnowledgeQueryFactoryTest {
    private final GraduateKnowledgeQueryFactory factory = new GraduateKnowledgeQueryFactory();

    @Test
    void buildsProgramQueryFromCanonicalDraftWithoutDatabaseResolution() {
        CanonicalGraduateQueryDraft draft = new CanonicalGraduateQueryDraft(
                2, "PROGRAM", "LIST",
                new CanonicalGraduateQueryDraft.Filters(
                        List.of("University of Balamand"), List.of("MASTER"), "Beirut",
                        "Business", null, null, List.of("English"), List.of("GMAT"),
                        List.of("business"), null),
                null, new CanonicalGraduateQueryDraft.Sort("NAME", "ASC"), null,
                "LIST", 5, false, List.of());

        GraduateKnowledgeQuery query = factory.create(
                draft,
                List.of(new ResolvedUniversity(22L, "University of Balamand", "UOB")),
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                false);

        assertEquals(22L, query.resolvedUniversities().get(0).id());
        assertEquals("Beirut", query.filters().city());
        assertEquals(GraduateKnowledgeOperation.LIST, query.operation());
        assertEquals(GraduateKnowledgeSortField.NAME, query.sort().field());
    }

    @Test
    void buildsTuitionAggregationFromCanonicalDraft() {
        CanonicalGraduateQueryDraft draft = new CanonicalGraduateQueryDraft(
                2, "PROGRAM", "AGGREGATE",
                new CanonicalGraduateQueryDraft.Filters(
                        List.of("AUB"), List.of("MASTER"), null,
                        null, null, "Master of Science in Computer Science",
                        List.of(), List.of(), List.of(), null),
                new CanonicalGraduateQueryDraft.Aggregation("AVG", "TUITION"),
                null, null, null, null, false, List.of());

        GraduateKnowledgeQuery query = factory.create(
                draft,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                null,
                false);

        assertEquals(GraduateKnowledgeIntent.TUITION_AGGREGATION, query.intent());
        assertEquals(GraduateKnowledgeOperation.AGGREGATE, query.operation());
        assertEquals(GraduateKnowledgeAggregationFunction.AVG, query.aggregation().function());
        assertEquals("tuition", query.aggregation().field());
        assertEquals(1L, query.resolvedUniversities().get(0).id());
        assertEquals(List.of("MASTER"), query.filters().degreeTypes());
    }

    @Test
    void buildsScopedLocationAndComparisonQueries() {
        CanonicalGraduateQueryDraft location = new CanonicalGraduateQueryDraft(
                2, "CAMPUS", "EXISTS",
                new CanonicalGraduateQueryDraft.Filters(List.of("LU"), List.of(), "Nabatieh", null, null, null,
                        List.of(), List.of(), List.of(), null),
                null, null, null, null, null, false, List.of());
        GraduateKnowledgeQuery locationQuery = factory.create(
                location, List.of(new ResolvedUniversity(11L, "Lebanese University", "UL")),
                GraduateKnowledgeIntent.LOCATION_LOOKUP, false);
        assertEquals(GraduateKnowledgeOperation.EXISTS, locationQuery.operation());
        assertEquals(11L, locationQuery.resolvedUniversities().get(0).id());

        CanonicalGraduateQueryDraft comparison = new CanonicalGraduateQueryDraft(
                2, "PROGRAM", "COMPARE",
                new CanonicalGraduateQueryDraft.Filters(List.of("AUB", "LAU"), List.of(), null, null, null, null,
                        List.of(), List.of(), List.of(), null),
                null, null, new CanonicalGraduateQueryDraft.Comparison("PROGRAM_COUNT"),
                "LIST", null, false, List.of());
        GraduateKnowledgeQuery comparisonQuery = factory.create(
                comparison, List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                GraduateKnowledgeIntent.PROGRAM_LOOKUP, false);
        assertEquals(GraduateKnowledgeComparisonDimension.PROGRAM_COUNT,
                comparisonQuery.followUpContext().comparisonDimension());
    }
}
