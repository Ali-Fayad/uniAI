package com.uniai.chat.application.planning;

import java.util.List;

/** Immutable planner metadata and the Java type accepted by one registered route. */
public record GraduateAiRouteDefinition<T>(
        GraduateAiRoute route,
        String purpose,
        Class<T> argumentType,
        List<String> requiredArguments,
        int maximumResultLimit,
        boolean enabled
) {
    public GraduateAiRouteDefinition {
        if (route == null || purpose == null || purpose.isBlank() || argumentType == null) {
            throw new IllegalArgumentException("Route metadata is incomplete");
        }
        requiredArguments = requiredArguments == null ? List.of() : List.copyOf(requiredArguments);
        if (maximumResultLimit < 0) {
            throw new IllegalArgumentException("Maximum result limit cannot be negative");
        }
    }
}
