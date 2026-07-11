package com.uniai.chat.application.interpretation;

import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;

import java.util.List;

public record GraduateQueryInterpretationResult(
        GraduateQueryInterpretationStatus status,
        GraduateKnowledgeQuery query,
        int resolvedUniversityCount,
        int degreeTypeCount,
        boolean ambiguous,
        boolean fallbackUsed,
        String safeMessage,
        String failureCategory,
        List<String> unsupportedConstraints
) {
    public GraduateQueryInterpretationResult {
        status = status == null ? GraduateQueryInterpretationStatus.INVALID : status;
        unsupportedConstraints = unsupportedConstraints == null ? List.of() : List.copyOf(unsupportedConstraints);
        safeMessage = safeMessage == null ? "" : safeMessage;
        failureCategory = failureCategory == null ? "" : failureCategory;
    }

    public static GraduateQueryInterpretationResult valid(GraduateKnowledgeQuery query, int resolvedUniversityCount, int degreeTypeCount) {
        return new GraduateQueryInterpretationResult(
                GraduateQueryInterpretationStatus.VALID,
                query,
                resolvedUniversityCount,
                degreeTypeCount,
                query != null && query.ambiguous(),
                false,
                "",
                "",
                List.of()
        );
    }

    public static GraduateQueryInterpretationResult ambiguous(String safeMessage, int resolvedUniversityCount, int degreeTypeCount, GraduateKnowledgeQuery query) {
        return new GraduateQueryInterpretationResult(
                GraduateQueryInterpretationStatus.AMBIGUOUS,
                query,
                resolvedUniversityCount,
                degreeTypeCount,
                true,
                false,
                safeMessage,
                "AI_QUERY_INTERPRETATION_AMBIGUOUS",
                List.of()
        );
    }

    public static GraduateQueryInterpretationResult unsupported(String safeMessage, int resolvedUniversityCount, int degreeTypeCount, List<String> unsupportedConstraints) {
        return new GraduateQueryInterpretationResult(
                GraduateQueryInterpretationStatus.UNSUPPORTED,
                null,
                resolvedUniversityCount,
                degreeTypeCount,
                false,
                false,
                safeMessage,
                "AI_QUERY_INTERPRETATION_UNSUPPORTED",
                unsupportedConstraints
        );
    }

    public static GraduateQueryInterpretationResult invalid(String failureCategory) {
        return new GraduateQueryInterpretationResult(
                GraduateQueryInterpretationStatus.INVALID,
                null,
                0,
                0,
                false,
                false,
                "",
                failureCategory,
                List.of()
        );
    }

    public static GraduateQueryInterpretationResult fallbackUsed(GraduateKnowledgeQuery query, String safeMessage) {
        int universityCount = query == null || query.resolvedUniversities() == null ? 0 : query.resolvedUniversities().size();
        int degreeCount = query == null || query.degreeTypes() == null ? 0 : query.degreeTypes().size();
        return new GraduateQueryInterpretationResult(
                GraduateQueryInterpretationStatus.FALLBACK_USED,
                query,
                universityCount,
                degreeCount,
                query != null && query.ambiguous(),
                true,
                safeMessage == null ? "" : safeMessage,
                "AI_QUERY_INTERPRETATION_FALLBACK_USED",
                List.of()
        );
    }
}
