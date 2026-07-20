package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.JsonNode;

/** Provider-facing planner contract. Only route and arguments are permitted. */
public record GraduateRoutePlan(
        GraduateAiRoute route,
        JsonNode arguments
) {
}
