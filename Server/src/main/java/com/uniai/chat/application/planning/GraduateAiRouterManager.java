package com.uniai.chat.application.planning;

import com.uniai.catalog.domain.model.UniversityCatalog;

import java.util.List;

/** Validates a strict plan, verifies handler/type parity, and dispatches one allow-listed route. */
public final class GraduateAiRouterManager {
    private final GraduateRoutePlanParser parser;
    private final GraduateAiRouteRegistry registry;
    private final GraduateRouteArgumentValidator argumentValidator;
    private final GraduateRouteEntityResolver entityResolver;

    public GraduateAiRouterManager(GraduateRoutePlanParser parser, GraduateAiRouteRegistry registry) {
        this(parser, registry, new GraduateRouteArgumentValidator(), new GraduateRouteEntityResolver());
    }

    public GraduateAiRouterManager(GraduateRoutePlanParser parser,
                                   GraduateAiRouteRegistry registry,
                                   GraduateRouteArgumentValidator argumentValidator,
                                   GraduateRouteEntityResolver entityResolver) {
        this.parser = parser;
        this.registry = registry;
        this.argumentValidator = argumentValidator;
        this.entityResolver = entityResolver;
    }

    public GraduateRouteExecutionResult execute(String rawPlan) {
        return execute(rawPlan, null, List.of());
    }

    public GraduateRouteExecutionResult execute(String rawPlan,
                                                String currentUserMessage,
                                                List<UniversityCatalog> universityCatalogs) {
        ValidatedGraduateRoutePlan<?> plan = parser.parse(rawPlan);
        return execute(plan, currentUserMessage, universityCatalogs);
    }

    public GraduateRouteExecutionResult execute(ValidatedGraduateRoutePlan<?> plan,
                                                String currentUserMessage,
                                                List<UniversityCatalog> universityCatalogs) {
        argumentValidator.validate(plan);
        ResolvedGraduateRoutePlan<?> resolvedPlan = entityResolver.resolve(
                plan, universityCatalogs, currentUserMessage);
        GraduateAiRouteHandler<?> handler = registry.handler(plan.route());
        if (!handler.argumentType().equals(plan.arguments().getClass())) {
            throw new GraduateRoutePlanningException("Route handler argument type mismatch: " + plan.route());
        }
        return executeTyped(handler, resolvedPlan).withResolvedUniversities(resolvedPlan.universities());
    }

    @SuppressWarnings("unchecked")
    private <T> GraduateRouteExecutionResult executeTyped(GraduateAiRouteHandler<?> untypedHandler,
                                                          ResolvedGraduateRoutePlan<?> plan) {
        GraduateAiRouteHandler<T> handler = (GraduateAiRouteHandler<T>) untypedHandler;
        return handler.executeResolved((ResolvedGraduateRoutePlan<T>) plan);
    }
}
