package com.uniai.chat.application.planning;

/** One explicit, read-only execution boundary for a registered graduate route. */
public interface GraduateAiRouteHandler<T> {
    GraduateAiRoute route();

    Class<T> argumentType();

    GraduateRouteExecutionResult execute(T arguments);

    default GraduateRouteExecutionResult executeResolved(ResolvedGraduateRoutePlan<T> plan) {
        return execute(plan.arguments());
    }
}
