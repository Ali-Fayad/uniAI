package com.uniai.chat.application.planning;

import com.uniai.chat.application.retrieval.GraduateKnowledgeAggregation;
import com.uniai.chat.application.retrieval.GraduateKnowledgeAggregationFunction;
import com.uniai.chat.application.retrieval.GraduateKnowledgeFilters;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeOperation;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeResource;
import com.uniai.chat.application.retrieval.GraduateKnowledgeSort;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraduateRoutePlannerShadowManagerTest {
    private final GraduateRoutePlannerShadowManager manager = new GraduateRoutePlannerShadowManager(
            false, null, null, null, Runnable::run);

    @Test
    void mapsLegacyProgramAndTuitionShapesForParityMetrics() {
        GraduateKnowledgeQuery programs = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                GraduateKnowledgeResource.PROGRAM,
                GraduateKnowledgeOperation.LIST,
                GraduateKnowledgeFilters.empty(),
                GraduateProgramDetailLevel.LIST,
                false,
                false);
        GraduateKnowledgeQuery tuition = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.TUITION_AGGREGATION,
                GraduateKnowledgeResource.PROGRAM,
                GraduateKnowledgeOperation.AGGREGATE,
                GraduateKnowledgeFilters.empty(),
                new GraduateKnowledgeAggregation(GraduateKnowledgeAggregationFunction.MAX, "tuition"),
                GraduateKnowledgeSort.empty(), null, null, null, false, false);

        assertEquals(GraduateAiRoute.LIST_PROGRAMS, manager.expectedRoute(programs));
        assertEquals(GraduateAiRoute.GET_MAXIMUM_TUITION, manager.expectedRoute(tuition));
    }
}
