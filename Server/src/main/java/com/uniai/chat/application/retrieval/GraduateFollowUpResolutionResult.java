package com.uniai.chat.application.retrieval;

import java.util.List;

public record GraduateFollowUpResolutionResult(
        GraduateFollowUpResolutionStatus status,
        GraduateKnowledgeQuery resolvedQuery,
        String clarificationReason,
        List<String> resolutionSources
) {
    public GraduateFollowUpResolutionResult {
        status = status == null ? GraduateFollowUpResolutionStatus.CLARIFICATION_REQUIRED : status;
        clarificationReason = clarificationReason == null ? "" : clarificationReason;
        resolutionSources = resolutionSources == null ? List.of() : List.copyOf(resolutionSources);
    }

    public static GraduateFollowUpResolutionResult resolved(GraduateKnowledgeQuery query, List<String> sources) {
        return new GraduateFollowUpResolutionResult(
                GraduateFollowUpResolutionStatus.RESOLVED,
                query,
                "",
                sources
        );
    }

    public static GraduateFollowUpResolutionResult unchanged(GraduateKnowledgeQuery query, List<String> sources) {
        return new GraduateFollowUpResolutionResult(
                GraduateFollowUpResolutionStatus.UNCHANGED,
                query,
                "",
                sources
        );
    }

    public static GraduateFollowUpResolutionResult clarificationRequired(String reason, GraduateKnowledgeQuery query, List<String> sources) {
        return new GraduateFollowUpResolutionResult(
                GraduateFollowUpResolutionStatus.CLARIFICATION_REQUIRED,
                query,
                reason,
                sources
        );
    }

    public static GraduateFollowUpResolutionResult unsupported(String reason) {
        return new GraduateFollowUpResolutionResult(
                GraduateFollowUpResolutionStatus.UNSUPPORTED,
                null,
                reason,
                List.of()
        );
    }
}
