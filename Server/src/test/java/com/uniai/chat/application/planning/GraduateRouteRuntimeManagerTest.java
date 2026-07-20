package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.budget.AiContextBudgetConfiguration;
import com.uniai.chat.application.budget.AiTokenEstimator;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetConfiguration;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetManager;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.port.out.GraduateRoutePlannerPort;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateRouteRuntimeManagerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void executesAValidatedDirectRouteAndBuildsFinalMetadataWithoutRetrieval() {
        GraduateRoutePlanParser parser = new GraduateRoutePlanParser(new GraduateAiRouteCatalog(), objectMapper);
        GraduateRoutePlannerPort planner = request -> parser.parse(
                "{\"route\":\"DIRECT_AI_RESPONSE\",\"arguments\":{\"reason\":\"GREETING\"}}");
        GraduateAiRouteRegistry registry = new GraduateAiRouteRegistry(
                List.of(new GraduateDirectAiRouteHandler(objectMapper)));
        GraduateRouteRuntimeManager manager = manager(true, planner,
                new GraduateAiRouterManager(parser, registry));

        GraduateRouteRuntimeOutcome outcome = manager.execute(
                "Hello", List.of(), ConversationMemory.empty(), List.of()).orElseThrow();

        assertEquals(GraduateAiRoute.DIRECT_AI_RESPONSE, outcome.executionResult().route());
        assertEquals(GraduateKnowledgeIntent.GENERAL_CHAT,
                outcome.memoryCompatibilityResult().query().intent());
        assertTrue(outcome.finalContext().contains("Graduate database retrieval: skipped"));
    }

    @Test
    void disabledRuntimeDoesNotCallThePlanner() {
        AtomicBoolean called = new AtomicBoolean();
        GraduateRoutePlannerPort planner = request -> {
            called.set(true);
            throw new AssertionError("planner must not run");
        };

        assertTrue(manager(false, planner, null).execute(
                "Hello", List.of(), ConversationMemory.empty(), List.of()).isEmpty());
        assertFalse(called.get());
    }

    @Test
    void plannerFailureFallsBackAtomicallyToTheLegacyRuntime() {
        GraduateRoutePlannerPort planner = request -> {
            throw new GraduateRoutePlanningException("bad plan");
        };

        assertTrue(manager(true, planner, null).execute(
                "Programs at AUB", List.of(), ConversationMemory.empty(), List.of()).isEmpty());
    }

    private GraduateRouteRuntimeManager manager(boolean enabled,
                                                GraduateRoutePlannerPort planner,
                                                GraduateAiRouterManager router) {
        AiTokenEstimator estimator = new AiTokenEstimator(new AiContextBudgetConfiguration(
                10000, 500, 1000, 1000, 4, 0, Map.of()));
        GraduateQueryInterpretationBudgetManager budget = new GraduateQueryInterpretationBudgetManager(
                new GraduateQueryInterpretationBudgetConfiguration(true, 10000, 500, 4, "unused"),
                estimator, "test");
        return new GraduateRouteRuntimeManager(enabled, planner, () -> "short prompt", budget,
                router, new GraduateRouteFinalContextBuilder());
    }
}
