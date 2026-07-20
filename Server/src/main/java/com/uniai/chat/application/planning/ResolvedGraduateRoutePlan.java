package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniai.chat.application.retrieval.ResolvedUniversity;

import java.util.List;

/** Typed route arguments plus server-resolved entity identity. */
public record ResolvedGraduateRoutePlan<T>(
        GraduateAiRoute route,
        T arguments,
        JsonNode canonicalArguments,
        List<ResolvedUniversity> universities,
        List<String> unresolvedUniversities
) {
    public ResolvedGraduateRoutePlan {
        universities = universities == null ? List.of() : List.copyOf(universities);
        unresolvedUniversities = unresolvedUniversities == null ? List.of() : List.copyOf(unresolvedUniversities);
    }

    public ResolvedGraduateRoutePlan(GraduateAiRoute route, T arguments, JsonNode canonicalArguments,
                                     List<ResolvedUniversity> universities) {
        this(route, arguments, canonicalArguments, universities, List.of());
    }
}
