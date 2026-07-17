package com.uniai.chat.application.retrieval;

import java.util.List;

public record GraduateKnowledgeQuery(
        GraduateKnowledgeIntent intent,
        GraduateKnowledgeResource resource,
        GraduateKnowledgeOperation operation,
        GraduateKnowledgeFilters filters,
        GraduateKnowledgeAggregation aggregation,
        GraduateKnowledgeSort sort,
        Integer limit,
        GraduateKnowledgeFollowUpContext followUpContext,
        GraduateProgramDetailLevel detailLevel,
        boolean followUpResolved,
        boolean ambiguous
) {
    public static final int DEFAULT_TUITION_LIMIT = 5;
    public static final int MAX_LIMIT = 20;

    public GraduateKnowledgeQuery {
        intent = intent == null ? GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS : intent;
        resource = resource == null ? resourceFor(intent) : resource;
        operation = operation == null ? operationFor(intent, detailLevel) : operation;
        filters = filters == null ? GraduateKnowledgeFilters.empty() : filters;
        aggregation = aggregation == null ? aggregationFor(intent) : aggregation;
        sort = sort == null ? GraduateKnowledgeSort.empty() : sort;
        followUpContext = followUpContext == null ? GraduateKnowledgeFollowUpContext.empty() : followUpContext;
        if (limit != null && (limit < 1 || limit > MAX_LIMIT)) {
            throw new IllegalArgumentException("Query limit must be positive");
        }
        if (!isCompatible(resource, operation)) {
            throw new IllegalArgumentException("Unsupported graduate resource/operation combination: "
                    + resource + "/" + operation);
        }
        if (intent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                && aggregation.function() != GraduateKnowledgeAggregationFunction.AVG
                && aggregation.function() != GraduateKnowledgeAggregationFunction.MIN
                && aggregation.function() != GraduateKnowledgeAggregationFunction.MAX
                && aggregation.function() != GraduateKnowledgeAggregationFunction.RANGE) {
            throw new IllegalArgumentException("Unsupported tuition aggregation function");
        }
        if (intent == GraduateKnowledgeIntent.GENERAL_CHAT
                || intent == GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS) {
            filters = GraduateKnowledgeFilters.empty();
        }
        if (detailLevel == null && intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP) {
            detailLevel = GraduateProgramDetailLevel.LIST;
        }
    }

    /** Compatibility constructor for existing callers and persisted-flow tests. */
    public GraduateKnowledgeQuery(
            GraduateKnowledgeIntent intent,
            List<ResolvedUniversity> resolvedUniversities,
            List<String> degreeTypes,
            GraduateProgramDetailLevel detailLevel,
            boolean followUpResolved,
            boolean ambiguous
    ) {
        this(
                intent,
                resourceFor(intent),
                operationFor(intent, detailLevel),
                new GraduateKnowledgeFilters(resolvedUniversities, degreeTypes, List.of()),
                aggregationFor(intent),
                GraduateKnowledgeSort.empty(),
                null,
                GraduateKnowledgeFollowUpContext.empty(),
                detailLevel,
                followUpResolved,
                ambiguous
        );
    }

    /** Compatibility constructor for callers that already use typed routing fields. */
    public GraduateKnowledgeQuery(
            GraduateKnowledgeIntent intent,
            GraduateKnowledgeResource resource,
            GraduateKnowledgeOperation operation,
            GraduateKnowledgeFilters filters,
            GraduateProgramDetailLevel detailLevel,
            boolean followUpResolved,
            boolean ambiguous
    ) {
        this(
                intent,
                resource,
                operation,
                filters,
                aggregationFor(intent),
                GraduateKnowledgeSort.empty(),
                null,
                GraduateKnowledgeFollowUpContext.empty(),
                detailLevel,
                followUpResolved,
                ambiguous
        );
    }

    public List<ResolvedUniversity> resolvedUniversities() {
        return filters.universities();
    }

    public List<String> degreeTypes() {
        return filters.degreeTypes();
    }

    public List<String> topicKeywords() {
        return filters.topicKeywords();
    }

    public boolean hasResolvedUniversities() {
        return !resolvedUniversities().isEmpty();
    }

    public boolean hasDegreeTypes() {
        return !degreeTypes().isEmpty();
    }

    public static GraduateKnowledgeResource resourceFor(GraduateKnowledgeIntent intent) {
        if (intent == null) {
            return GraduateKnowledgeResource.NONE;
        }
        return switch (intent) {
            case PROGRAM_LOOKUP, TUITION_AGGREGATION -> GraduateKnowledgeResource.PROGRAM;
            case GRADUATE_OVERVIEW -> GraduateKnowledgeResource.GRADUATE_OVERVIEW;
            case LOCATION_LOOKUP -> GraduateKnowledgeResource.CAMPUS;
            case ACADEMIC_STRUCTURE_LOOKUP, GENERAL_CHAT, UNKNOWN_OR_AMBIGUOUS -> GraduateKnowledgeResource.NONE;
        };
    }

    public static GraduateKnowledgeOperation operationFor(
            GraduateKnowledgeIntent intent,
            GraduateProgramDetailLevel detailLevel
    ) {
        if (intent == null) {
            return GraduateKnowledgeOperation.NONE;
        }
        return switch (intent) {
            case PROGRAM_LOOKUP -> detailLevel == GraduateProgramDetailLevel.DETAILS
                    ? GraduateKnowledgeOperation.DETAILS
                    : GraduateKnowledgeOperation.LIST;
            case TUITION_AGGREGATION -> GraduateKnowledgeOperation.AGGREGATE;
            case GRADUATE_OVERVIEW -> GraduateKnowledgeOperation.OVERVIEW;
            case LOCATION_LOOKUP -> GraduateKnowledgeOperation.LIST;
            case ACADEMIC_STRUCTURE_LOOKUP, GENERAL_CHAT, UNKNOWN_OR_AMBIGUOUS -> GraduateKnowledgeOperation.NONE;
        };
    }

    public static GraduateKnowledgeAggregation aggregationFor(GraduateKnowledgeIntent intent) {
        return intent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                ? new GraduateKnowledgeAggregation(GraduateKnowledgeAggregationFunction.AVG, "tuition")
                : GraduateKnowledgeAggregation.empty();
    }

    public static boolean isCompatible(
            GraduateKnowledgeResource resource,
            GraduateKnowledgeOperation operation
    ) {
        if (resource == null || operation == null) {
            return false;
        }
        if (resource == GraduateKnowledgeResource.NONE) {
            return operation == GraduateKnowledgeOperation.NONE;
        }
        if (resource == GraduateKnowledgeResource.GRADUATE_OVERVIEW) {
            return operation == GraduateKnowledgeOperation.OVERVIEW;
        }
        if (resource == GraduateKnowledgeResource.FACULTY || resource == GraduateKnowledgeResource.DEPARTMENT) {
            return operation == GraduateKnowledgeOperation.LIST
                    || operation == GraduateKnowledgeOperation.COUNT
                    || operation == GraduateKnowledgeOperation.EXISTS;
        }
        if (operation == GraduateKnowledgeOperation.OVERVIEW
                || operation == GraduateKnowledgeOperation.NONE) {
            return false;
        }
        return true;
    }
}
