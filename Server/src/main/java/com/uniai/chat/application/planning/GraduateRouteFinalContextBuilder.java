package com.uniai.chat.application.planning;

import com.uniai.chat.application.citation.GraduateCitation;

/** Builds the trusted route envelope supplied to the final answer-generation call. */
public final class GraduateRouteFinalContextBuilder {
    public String build(GraduateRouteExecutionResult result) {
        if (result == null) return "";
        StringBuilder context = new StringBuilder("Graduate route execution:\n")
                .append("Selected route: ").append(result.route()).append('\n')
                .append("Validated canonical arguments: ")
                .append(result.canonicalArguments() == null ? "{}" : result.canonicalArguments()).append('\n');
        if (result.route() == GraduateAiRoute.DIRECT_AI_RESPONSE) {
            context.append("Direct AI reason: ").append(result.directAiReason()).append('\n')
                    .append("Graduate database retrieval: skipped");
            return context.toString();
        }
        context.append("Empty result: ").append(result.empty()).append('\n');
        if (!result.warnings().isEmpty()) {
            context.append("Warnings:\n");
            result.warnings().forEach(warning -> context.append("- ").append(warning).append('\n'));
        }
        context.append("Retrieved database context:\n")
                .append(result.formattedContext().isBlank() ? "No matching structured data." : result.formattedContext());
        if (!result.citations().isEmpty()) {
            context.append("\nSource references:\n");
            for (GraduateCitation citation : result.citations()) {
                context.append("- [").append(citation.label()).append("] ")
                        .append(citation.title());
                if (!citation.universityName().isBlank()) {
                    context.append(" | University: ").append(citation.universityName());
                }
                if (!citation.url().isBlank()) context.append(" | URL: ").append(citation.url());
                context.append('\n');
            }
        }
        return context.toString().trim();
    }
}
