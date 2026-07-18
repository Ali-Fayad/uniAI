package com.uniai.chat.application.retrieval;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraduateKnowledgeQueryTest {

    @Test
    void legacyProgramQueryMapsToTypedProgramList() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                false,
                false
        );

        assertEquals(GraduateKnowledgeResource.PROGRAM, query.resource());
        assertEquals(GraduateKnowledgeOperation.LIST, query.operation());
        assertEquals(List.of("MASTER"), query.degreeTypes());
        assertEquals("AUB", query.resolvedUniversities().get(0).acronym());
    }

    @Test
    void graduateOverviewRemainsACompositeRoute() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.GRADUATE_OVERVIEW,
                List.of(),
                List.of(),
                null,
                false,
                false
        );

        assertEquals(GraduateKnowledgeResource.GRADUATE_OVERVIEW, query.resource());
        assertEquals(GraduateKnowledgeOperation.OVERVIEW, query.operation());
    }

    @Test
    void generalChatHasNoRetrievalResource() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.GENERAL_CHAT,
                List.of(),
                List.of(),
                null,
                false,
                false
        );

        assertEquals(GraduateKnowledgeResource.NONE, query.resource());
        assertEquals(GraduateKnowledgeOperation.NONE, query.operation());
        assertEquals(GraduateKnowledgeFilters.empty(), query.filters());
    }

    @Test
    void invalidResourceOperationCombinationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                GraduateKnowledgeResource.GRADUATE_OVERVIEW,
                GraduateKnowledgeOperation.LIST,
                new GraduateKnowledgeFilters(List.of(), List.of(), List.of()),
                GraduateProgramDetailLevel.LIST,
                false,
                false
        ));
    }

    @Test
    void compareRequiresObjectiveDimension() {
        GraduateKnowledgeFollowUpContext context = new GraduateKnowledgeFollowUpContext(
                null, null, GraduateKnowledgeResource.PROGRAM, GraduateKnowledgeOperation.COMPARE,
                List.of(new GraduateKnowledgeReference(GraduateKnowledgeReferenceKind.UNIVERSITY, "AUB", "AUB", 1)),
                GraduateKnowledgeComparisonDimension.PROGRAM_COUNT
        );
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                GraduateKnowledgeResource.PROGRAM,
                GraduateKnowledgeOperation.COMPARE,
                new GraduateKnowledgeFilters(List.of(), List.of("MASTER"), List.of()),
                GraduateKnowledgeAggregation.empty(), GraduateKnowledgeSort.empty(), 5,
                context, GraduateProgramDetailLevel.LIST, true, false
        );

        assertEquals(GraduateKnowledgeComparisonDimension.PROGRAM_COUNT, query.followUpContext().comparisonDimension());
        assertThrows(IllegalArgumentException.class, () -> new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                GraduateKnowledgeResource.PROGRAM,
                GraduateKnowledgeOperation.COMPARE,
                GraduateKnowledgeFilters.empty(), GraduateKnowledgeAggregation.empty(), GraduateKnowledgeSort.empty(),
                5, GraduateKnowledgeFollowUpContext.empty(), GraduateProgramDetailLevel.LIST, true, false
        ));
    }
}
