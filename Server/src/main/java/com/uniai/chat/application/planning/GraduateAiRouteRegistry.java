package com.uniai.chat.application.planning;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Explicit handler allow-list; route names are never reflected into method calls. */
public final class GraduateAiRouteRegistry {
    private final Map<GraduateAiRoute, GraduateAiRouteHandler<?>> handlers;

    public GraduateAiRouteRegistry(Collection<GraduateAiRouteHandler<?>> routeHandlers) {
        EnumMap<GraduateAiRoute, GraduateAiRouteHandler<?>> registered = new EnumMap<>(GraduateAiRoute.class);
        if (routeHandlers != null) {
            for (GraduateAiRouteHandler<?> handler : routeHandlers) {
                if (handler == null || handler.route() == null || handler.argumentType() == null) {
                    throw new IllegalArgumentException("Graduate route handler metadata is incomplete");
                }
                if (registered.put(handler.route(), handler) != null) {
                    throw new IllegalStateException("Duplicate graduate route handler: " + handler.route());
                }
            }
        }
        handlers = Collections.unmodifiableMap(registered);
    }

    public GraduateAiRouteHandler<?> handler(GraduateAiRoute route) {
        GraduateAiRouteHandler<?> handler = handlers.get(route);
        if (handler == null) throw new GraduateRoutePlanningException("No enabled handler for route: " + route);
        return handler;
    }

    public boolean contains(GraduateAiRoute route) {
        return handlers.containsKey(route);
    }

    public Map<GraduateAiRoute, GraduateAiRouteHandler<?>> handlers() {
        return handlers;
    }
}
