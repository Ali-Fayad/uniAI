package com.uniai.chat.application.planning;

import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetManager;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetResult;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.port.out.GraduateRoutePlannerPort;
import com.uniai.chat.application.port.out.GraduateRoutePlannerPromptPort;
import com.uniai.chat.application.retrieval.GraduateKnowledgeAggregationFunction;
import com.uniai.chat.application.retrieval.GraduateKnowledgeComparisonDimension;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeOperation;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeResource;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.Executor;

/** Optional, non-authoritative old/new planner parity runner used before cutover. */
public final class GraduateRoutePlannerShadowManager {
    private static final Logger logger = LogManager.getLogger(GraduateRoutePlannerShadowManager.class);
    private final boolean enabled;
    private final GraduateRoutePlannerPort plannerPort;
    private final GraduateRoutePlannerPromptPort promptPort;
    private final GraduateQueryInterpretationBudgetManager budgetManager;
    private final Executor executor;

    public GraduateRoutePlannerShadowManager(boolean enabled,
                                             GraduateRoutePlannerPort plannerPort,
                                             GraduateRoutePlannerPromptPort promptPort,
                                             GraduateQueryInterpretationBudgetManager budgetManager,
                                             Executor executor) {
        this.enabled = enabled;
        this.plannerPort = plannerPort;
        this.promptPort = promptPort;
        this.budgetManager = budgetManager;
        this.executor = executor;
    }

    public void submit(String currentMessage,
                       List<AiConversationMessage> recentHistory,
                       ConversationMemory memory,
                       GraduateKnowledgeQuery legacyQuery) {
        if (!enabled || legacyQuery == null) return;
        GraduateQueryInterpretationRequest request = new GraduateQueryInterpretationRequest(
                currentMessage, recentHistory, memory);
        GraduateQueryInterpretationBudgetResult budget = budgetManager.budget(request, promptPort.getPrompt());
        if (!budget.requestFits()) {
            logger.info("[AI_ROUTE_PLANNER_SHADOW] Skipped reason=budget_rejected expectedRoute={}",
                    expectedRoute(legacyQuery));
            return;
        }
        executor.execute(() -> compare(budget.request(), legacyQuery));
    }

    private void compare(GraduateQueryInterpretationRequest request, GraduateKnowledgeQuery legacyQuery) {
        GraduateAiRoute expected = expectedRoute(legacyQuery);
        try {
            ValidatedGraduateRoutePlan<?> plan = plannerPort.plan(request);
            logger.info("[AI_ROUTE_PLANNER_SHADOW] Completed expectedRoute={} plannedRoute={} agreement={}",
                    expected, plan.route(), expected == plan.route());
        } catch (RuntimeException ex) {
            logger.warn("[AI_ROUTE_PLANNER_SHADOW] Failed expectedRoute={} failureType={}",
                    expected, ex.getClass().getSimpleName());
        }
    }

    GraduateAiRoute expectedRoute(GraduateKnowledgeQuery query) {
        if (query.intent() == GraduateKnowledgeIntent.GENERAL_CHAT) return GraduateAiRoute.DIRECT_AI_RESPONSE;
        if (query.intent() == GraduateKnowledgeIntent.GRADUATE_OVERVIEW) return GraduateAiRoute.GET_GRADUATE_OVERVIEW;
        if (query.intent() == GraduateKnowledgeIntent.TUITION_AGGREGATION) {
            if (query.operation() == GraduateKnowledgeOperation.COMPARE) return GraduateAiRoute.COMPARE_TUITION;
            GraduateKnowledgeAggregationFunction function = query.aggregation().function();
            if (function == GraduateKnowledgeAggregationFunction.MIN) return GraduateAiRoute.GET_MINIMUM_TUITION;
            if (function == GraduateKnowledgeAggregationFunction.MAX) return GraduateAiRoute.GET_MAXIMUM_TUITION;
            return GraduateAiRoute.GET_AVERAGE_TUITION;
        }
        if (query.resource() == GraduateKnowledgeResource.CAMPUS) {
            if (query.operation() == GraduateKnowledgeOperation.COUNT) return GraduateAiRoute.COUNT_CAMPUSES;
            if (query.operation() == GraduateKnowledgeOperation.EXISTS) return GraduateAiRoute.CHECK_CAMPUS_EXISTS;
            if (query.operation() == GraduateKnowledgeOperation.COMPARE) return GraduateAiRoute.COMPARE_CAMPUS_COUNTS;
            return GraduateAiRoute.LIST_CAMPUSES;
        }
        if (query.resource() == GraduateKnowledgeResource.UNIVERSITY) {
            if (query.operation() == GraduateKnowledgeOperation.COUNT) return GraduateAiRoute.COUNT_UNIVERSITIES;
            if (query.operation() == GraduateKnowledgeOperation.COMPARE) return GraduateAiRoute.COMPARE_UNIVERSITIES;
            return GraduateAiRoute.LIST_UNIVERSITIES;
        }
        if (query.resource() == GraduateKnowledgeResource.FACULTY) {
            return query.operation() == GraduateKnowledgeOperation.COUNT
                    ? GraduateAiRoute.COUNT_FACULTIES : GraduateAiRoute.LIST_FACULTIES;
        }
        if (query.resource() == GraduateKnowledgeResource.DEPARTMENT) {
            return query.operation() == GraduateKnowledgeOperation.COUNT
                    ? GraduateAiRoute.COUNT_DEPARTMENTS : GraduateAiRoute.LIST_DEPARTMENTS;
        }
        if (query.resource() == GraduateKnowledgeResource.PROGRAM) {
            if (query.operation() == GraduateKnowledgeOperation.COUNT) return GraduateAiRoute.COUNT_PROGRAMS;
            if (query.operation() == GraduateKnowledgeOperation.EXISTS) return GraduateAiRoute.CHECK_PROGRAM_EXISTS;
            if (query.operation() == GraduateKnowledgeOperation.COMPARE) {
                GraduateKnowledgeComparisonDimension dimension = query.followUpContext().comparisonDimension();
                return dimension == GraduateKnowledgeComparisonDimension.PROGRAM_AVAILABILITY
                        ? GraduateAiRoute.COMPARE_PROGRAM_AVAILABILITY : GraduateAiRoute.COMPARE_PROGRAM_COUNTS;
            }
            return query.detailLevel() == GraduateProgramDetailLevel.DETAILS
                    ? GraduateAiRoute.GET_PROGRAM_DETAILS : GraduateAiRoute.LIST_PROGRAMS;
        }
        return null;
    }
}
