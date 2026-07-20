package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.JsonNode;
import com.uniai.chat.application.citation.GraduateCitation;
import com.uniai.chat.application.retrieval.ResolvedUniversity;

import java.util.List;

/** Normalized output from route validation and execution. */
public record GraduateRouteExecutionResult(
        GraduateAiRoute route,
        JsonNode canonicalArguments,
        String formattedContext,
        List<GraduateCitation> citations,
        List<String> warnings,
        boolean empty,
        List<ResolvedUniversity> resolvedUniversities,
        GraduateDirectAiReason directAiReason
) {
    public GraduateRouteExecutionResult {
        formattedContext = formattedContext == null ? "" : formattedContext;
        citations = citations == null ? List.of() : List.copyOf(citations);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        resolvedUniversities = resolvedUniversities == null ? List.of() : List.copyOf(resolvedUniversities);
    }

    public GraduateRouteExecutionResult(GraduateAiRoute route,
                                        JsonNode canonicalArguments,
                                        String formattedContext,
                                        List<GraduateCitation> citations,
                                        List<String> warnings,
                                        boolean empty,
                                        GraduateDirectAiReason directAiReason) {
        this(route, canonicalArguments, formattedContext, citations, warnings, empty, List.of(), directAiReason);
    }

    public static GraduateRouteExecutionResult direct(JsonNode arguments, GraduateDirectAiReason reason) {
        return new GraduateRouteExecutionResult(
                GraduateAiRoute.DIRECT_AI_RESPONSE, arguments, "", List.of(), List.of(), true, List.of(), reason);
    }

    public GraduateRouteExecutionResult withResolvedUniversities(List<ResolvedUniversity> universities) {
        return new GraduateRouteExecutionResult(route, canonicalArguments, formattedContext, citations,
                warnings, empty, universities, directAiReason);
    }
}
