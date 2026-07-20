package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.JsonNode;

public record ValidatedGraduateRoutePlan<T>(
        GraduateAiRoute route,
        T arguments,
        JsonNode canonicalArguments
) {
}
