package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetManager;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetResult;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationResult;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.port.out.GraduateRoutePlannerPort;
import com.uniai.chat.application.port.out.GraduateRoutePlannerPromptPort;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Feature-gated production seam for the route planner.
 *
 * <p>A failure returns no outcome so orchestration can atomically use the legacy
 * interpreter during rollout. No partially executed route result is reused.</p>
 */
public class GraduateRouteRuntimeManager {
    private static final Logger logger = LogManager.getLogger(GraduateRouteRuntimeManager.class);

    private final boolean enabled;
    private final GraduateRoutePlannerPort plannerPort;
    private final GraduateRoutePlannerPromptPort promptPort;
    private final GraduateQueryInterpretationBudgetManager budgetManager;
    private final GraduateAiRouterManager routerManager;
    private final GraduateRouteFinalContextBuilder contextBuilder;

    public GraduateRouteRuntimeManager(boolean enabled,
                                       GraduateRoutePlannerPort plannerPort,
                                       GraduateRoutePlannerPromptPort promptPort,
                                       GraduateQueryInterpretationBudgetManager budgetManager,
                                       GraduateAiRouterManager routerManager,
                                       GraduateRouteFinalContextBuilder contextBuilder) {
        this.enabled = enabled;
        this.plannerPort = plannerPort;
        this.promptPort = promptPort;
        this.budgetManager = budgetManager;
        this.routerManager = routerManager;
        this.contextBuilder = contextBuilder;
    }

    public Optional<GraduateRouteRuntimeOutcome> execute(String currentMessage,
                                                         List<AiConversationMessage> recentHistory,
                                                         ConversationMemory memory,
                                                         List<UniversityCatalog> universityCatalogs) {
        if (!enabled) return Optional.empty();
        GraduateQueryInterpretationRequest request = new GraduateQueryInterpretationRequest(
                currentMessage, recentHistory, memory);
        GraduateQueryInterpretationBudgetResult budget = budgetManager.budget(request, promptPort.getPrompt());
        if (!budget.requestFits()) {
            logger.warn("[AI_ROUTE_PLANNER] Runtime fallback reason=budget_rejected category={}",
                    budget.diagnosticCategory());
            return Optional.empty();
        }
        try {
            ValidatedGraduateRoutePlan<?> plan = plannerPort.plan(budget.request());
            GraduateRouteExecutionResult result = routerManager.execute(
                    plan, currentMessage, universityCatalogs);
            logger.info("[AI_ROUTE_PLANNER] Runtime execution completed route={} empty={} citationCount={}",
                    result.route(), result.empty(), result.citations().size());
            return Optional.of(new GraduateRouteRuntimeOutcome(
                    result,
                    contextBuilder.build(result),
                    compatibilityResult(result)));
        } catch (RuntimeException ex) {
            logger.warn("[AI_ROUTE_PLANNER] Runtime fallback reason=planning_or_execution_failed failureType={}",
                    ex.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    private GraduateQueryInterpretationResult compatibilityResult(GraduateRouteExecutionResult result) {
        GraduateKnowledgeIntent intent = legacyIntent(result.route());
        List<String> degreeTypes = degreeTypes(result.canonicalArguments());
        GraduateProgramDetailLevel detailLevel = result.route() == GraduateAiRoute.GET_PROGRAM_DETAILS
                ? GraduateProgramDetailLevel.DETAILS
                : intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP ? GraduateProgramDetailLevel.LIST : null;
        GraduateKnowledgeQuery memoryQuery = new GraduateKnowledgeQuery(
                intent, result.resolvedUniversities(), degreeTypes, detailLevel, false, false);
        return GraduateQueryInterpretationResult.valid(
                memoryQuery, result.resolvedUniversities().size(), degreeTypes.size());
    }

    private GraduateKnowledgeIntent legacyIntent(GraduateAiRoute route) {
        if (route == GraduateAiRoute.DIRECT_AI_RESPONSE) return GraduateKnowledgeIntent.GENERAL_CHAT;
        if (route == GraduateAiRoute.GET_GRADUATE_OVERVIEW) return GraduateKnowledgeIntent.GRADUATE_OVERVIEW;
        if (route.name().contains("TUITION") || route == GraduateAiRoute.LIST_FEE_ITEMS) {
            return GraduateKnowledgeIntent.TUITION_AGGREGATION;
        }
        if (route.name().contains("CAMPUS") || route == GraduateAiRoute.LIST_UNIVERSITIES_BY_CITY) {
            return GraduateKnowledgeIntent.LOCATION_LOOKUP;
        }
        if (route.name().contains("FACULT") || route.name().contains("DEPARTMENT")) {
            return GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP;
        }
        return GraduateKnowledgeIntent.PROGRAM_LOOKUP;
    }

    private List<String> degreeTypes(JsonNode arguments) {
        if (arguments == null) return List.of();
        List<String> result = new ArrayList<>();
        JsonNode degree = arguments.get("degreeType");
        if (degree != null && degree.isTextual()) result.add(degree.textValue());
        JsonNode degrees = arguments.get("degreeTypes");
        if (degrees != null && degrees.isArray()) {
            degrees.forEach(value -> {
                if (value.isTextual()) result.add(value.textValue());
            });
        }
        return List.copyOf(result);
    }
}
