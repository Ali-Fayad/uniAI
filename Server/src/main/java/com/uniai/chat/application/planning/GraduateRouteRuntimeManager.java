package com.uniai.chat.application.planning;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.chat.application.budget.GraduateRoutePlannerBudgetManager;
import com.uniai.chat.application.budget.GraduateRoutePlannerBudgetResult;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.port.out.GraduateRoutePlannerPort;
import com.uniai.chat.application.port.out.GraduateRoutePlannerPromptPort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Production orchestration seam for the route planner.
 */
public class GraduateRouteRuntimeManager {
    private static final Logger logger = LogManager.getLogger(GraduateRouteRuntimeManager.class);

    private final GraduateRoutePlannerPort plannerPort;
    private final GraduateRoutePlannerPromptPort promptPort;
    private final GraduateRoutePlannerBudgetManager budgetManager;
    private final GraduateAiRouterManager routerManager;
    private final GraduateRouteFinalContextBuilder contextBuilder;
    private final GraduateRouteDeterministicFallback deterministicFallback;

    public GraduateRouteRuntimeManager(GraduateRoutePlannerPort plannerPort,
                                       GraduateRoutePlannerPromptPort promptPort,
                                       GraduateRoutePlannerBudgetManager budgetManager,
                                       GraduateAiRouterManager routerManager,
                                       GraduateRouteFinalContextBuilder contextBuilder) {
        this(plannerPort, promptPort, budgetManager, routerManager, contextBuilder,
                new GraduateRouteDeterministicFallback(
                        new GraduateRoutePlanParser(new GraduateAiRouteCatalog(), new com.fasterxml.jackson.databind.ObjectMapper())));
    }

    public GraduateRouteRuntimeManager(GraduateRoutePlannerPort plannerPort,
                                       GraduateRoutePlannerPromptPort promptPort,
                                       GraduateRoutePlannerBudgetManager budgetManager,
                                       GraduateAiRouterManager routerManager,
                                       GraduateRouteFinalContextBuilder contextBuilder,
                                       GraduateRouteDeterministicFallback deterministicFallback) {
        this.plannerPort = plannerPort;
        this.promptPort = promptPort;
        this.budgetManager = budgetManager;
        this.routerManager = routerManager;
        this.contextBuilder = contextBuilder;
        this.deterministicFallback = deterministicFallback;
    }

    public GraduateRouteRuntimeOutcome execute(String currentMessage,
                                                         List<AiConversationMessage> recentHistory,
                                                         ConversationMemory memory,
                                                         List<UniversityCatalog> universityCatalogs) {
        GraduateRoutePlanningRequest request = new GraduateRoutePlanningRequest(
                currentMessage, recentHistory, memory);
        GraduateRoutePlannerBudgetResult budget = budgetManager.budget(request, promptPort.getPrompt());
        if (!budget.requestFits()) {
            logger.warn("[AI_ROUTE_PLANNER] Runtime rejected reason=budget_rejected category={}",
                    budget.diagnosticCategory());
            throw new GraduateRoutePlanningException("Route planner request exceeds its configured budget");
        }
        try {
            ValidatedGraduateRoutePlan<?> plan;
            try {
                plan = plannerPort.plan(budget.request());
            } catch (RuntimeException plannerFailure) {
                logger.warn("[AI_ROUTE_PLANNER] Deterministic fallback attempted reason={}",
                        plannerFailure.getClass().getSimpleName());
                plan = deterministicFallback.plan(currentMessage, universityCatalogs)
                        .map(fallbackPlan -> {
                            logger.warn("[AI_ROUTE_PLANNER] Deterministic fallback succeeded route={}",
                                    fallbackPlan.route());
                            return fallbackPlan;
                        })
                        .orElseThrow(() -> plannerFailure);
            }
            GraduateRouteExecutionResult result = routerManager.execute(
                    plan, currentMessage, universityCatalogs);
            logger.info("[AI_ROUTE_PLANNER] Runtime execution completed route={} empty={} citationCount={}",
                    result.route(), result.empty(), result.citations().size());
            return new GraduateRouteRuntimeOutcome(result, contextBuilder.build(result));
        } catch (RuntimeException ex) {
            logger.warn("[AI_ROUTE_PLANNER] Runtime failed failureType={}",
                    ex.getClass().getSimpleName());
            throw ex;
        }
    }
}
