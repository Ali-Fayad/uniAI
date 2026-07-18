package com.uniai.chat.application.retrieval;

import java.util.List;

public record GraduateKnowledgeQuery(
        GraduateKnowledgeIntent intent,
        GraduateKnowledgeResource resource,
        GraduateKnowledgeOperation operation,
        GraduateKnowledgeScope scope,
        GraduateKnowledgeFilters filters,
        GraduateKnowledgeAggregation aggregation,
        GraduateKnowledgeSort sort,
        Integer limit,
        GraduateKnowledgeFollowUpContext followUpContext,
        GraduateProgramDetailLevel detailLevel,
        boolean followUpResolved,
        boolean ambiguous,
        GraduateKnowledgeInterpretationSource interpretationSource,
        GraduateKnowledgeAmbiguityReason ambiguityReason
) {
    public static final int DEFAULT_TUITION_LIMIT = 5;
    public static final int MAX_LIMIT = 20;

    public GraduateKnowledgeQuery {
        intent = intent == null ? GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS : intent;
        resource = resource == null ? resourceFor(intent) : resource;
        operation = operation == null ? operationFor(intent, detailLevel) : operation;
        if (intent == GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS) {
            intent = deriveIntent(resource, operation);
        }
        scope = scope == null ? scopeFor(resource, intent) : scope;
        filters = filters == null ? GraduateKnowledgeFilters.empty() : filters;
        aggregation = aggregation == null ? aggregationFor(intent) : aggregation;
        sort = sort == null ? GraduateKnowledgeSort.empty() : sort;
        followUpContext = followUpContext == null ? GraduateKnowledgeFollowUpContext.empty() : followUpContext;
        interpretationSource = interpretationSource == null ? GraduateKnowledgeInterpretationSource.DETERMINISTIC : interpretationSource;
        ambiguityReason = ambiguityReason == null
                ? (ambiguous ? GraduateKnowledgeAmbiguityReason.MISSING_REQUIRED_SCOPE : GraduateKnowledgeAmbiguityReason.NONE)
                : ambiguityReason;
        if (limit != null && (limit < 1 || limit > MAX_LIMIT)) {
            throw new IllegalArgumentException("Query limit must be positive");
        }
        if (!isCompatible(resource, operation)) {
            throw new IllegalArgumentException("Unsupported graduate resource/operation combination: "
                    + resource + "/" + operation);
        }
        if (!isScopeCompatible(resource, scope)) {
            throw new IllegalArgumentException("Unsupported graduate resource/scope combination: " + resource + "/" + scope);
        }
        if (operation == GraduateKnowledgeOperation.COMPARE
                && followUpContext.comparisonDimension() == null) {
            throw new IllegalArgumentException("Objective comparison dimension is required");
        }
        if (intent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                && aggregation.function() != GraduateKnowledgeAggregationFunction.AVG
                && aggregation.function() != GraduateKnowledgeAggregationFunction.MIN
                && aggregation.function() != GraduateKnowledgeAggregationFunction.MAX
                && aggregation.function() != GraduateKnowledgeAggregationFunction.RANGE) {
            throw new IllegalArgumentException("Unsupported tuition aggregation function");
        }
        if (intent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                && aggregation.function() == GraduateKnowledgeAggregationFunction.RANGE
                && filters.thresholdOperator() != GraduateKnowledgeThresholdOperator.NONE) {
            throw new IllegalArgumentException("RANGE cannot be combined with a tuition threshold");
        }
        if (intent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                && hasProgramScopedFilters(filters)
                && filters.tuitionScopeLevel() != null
                && !"PROGRAM".equals(filters.tuitionScopeLevel())) {
            throw new IllegalArgumentException("Program filters require PROGRAM tuition scope");
        }
        if (resource == GraduateKnowledgeResource.NONE && operation == GraduateKnowledgeOperation.NONE) {
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
                scopeFor(resourceFor(intent), intent),
                new GraduateKnowledgeFilters(resolvedUniversities, degreeTypes, List.of()),
                aggregationFor(intent),
                GraduateKnowledgeSort.empty(),
                null,
                GraduateKnowledgeFollowUpContext.empty(),
                detailLevel,
                followUpResolved,
                ambiguous,
                GraduateKnowledgeInterpretationSource.DETERMINISTIC,
                ambiguous ? GraduateKnowledgeAmbiguityReason.MISSING_REQUIRED_SCOPE : GraduateKnowledgeAmbiguityReason.NONE
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
                scopeFor(resource, intent),
                filters,
                aggregationFor(intent),
                GraduateKnowledgeSort.empty(),
                null,
                GraduateKnowledgeFollowUpContext.empty(),
                detailLevel,
                followUpResolved,
                ambiguous,
                GraduateKnowledgeInterpretationSource.DETERMINISTIC,
                ambiguous ? GraduateKnowledgeAmbiguityReason.MISSING_REQUIRED_SCOPE : GraduateKnowledgeAmbiguityReason.NONE
        );
    }

    /** Compatibility constructor for the pre-metadata normalized shape. */
    public GraduateKnowledgeQuery(
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
        this(intent, resource, operation, scopeFor(resource, intent), filters, aggregation, sort, limit,
                followUpContext, detailLevel, followUpResolved, ambiguous,
                GraduateKnowledgeInterpretationSource.DETERMINISTIC,
                ambiguous ? GraduateKnowledgeAmbiguityReason.MISSING_REQUIRED_SCOPE : GraduateKnowledgeAmbiguityReason.NONE);
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

    public GraduateKnowledgeQuery withDecisionMetadata(
            GraduateKnowledgeInterpretationSource source,
            GraduateKnowledgeAmbiguityReason reason,
            boolean ambiguous
    ) {
        return new GraduateKnowledgeQuery(intent, resource, operation, scope, filters, aggregation, sort, limit,
                followUpContext, detailLevel, followUpResolved, ambiguous, source, reason);
    }

    public static GraduateKnowledgeScope scopeFor(GraduateKnowledgeResource resource, GraduateKnowledgeIntent intent) {
        if (resource == GraduateKnowledgeResource.GRADUATE_OVERVIEW) return GraduateKnowledgeScope.GRADUATE_OVERVIEW;
        if (resource == GraduateKnowledgeResource.CAMPUS) return GraduateKnowledgeScope.CAMPUS;
        if (resource == GraduateKnowledgeResource.FACULTY) return GraduateKnowledgeScope.FACULTY;
        if (resource == GraduateKnowledgeResource.DEPARTMENT) return GraduateKnowledgeScope.DEPARTMENT;
        if (resource == GraduateKnowledgeResource.PROGRAM) return GraduateKnowledgeScope.PROGRAM;
        if (resource == GraduateKnowledgeResource.UNIVERSITY) return GraduateKnowledgeScope.UNIVERSITY;
        return intent == GraduateKnowledgeIntent.GENERAL_CHAT ? GraduateKnowledgeScope.GENERAL : GraduateKnowledgeScope.GENERAL;
    }

    public static GraduateKnowledgeIntent deriveIntent(GraduateKnowledgeResource resource, GraduateKnowledgeOperation operation) {
        if (resource == GraduateKnowledgeResource.GRADUATE_OVERVIEW && operation == GraduateKnowledgeOperation.OVERVIEW) return GraduateKnowledgeIntent.GRADUATE_OVERVIEW;
        if (resource == GraduateKnowledgeResource.PROGRAM && operation == GraduateKnowledgeOperation.AGGREGATE) return GraduateKnowledgeIntent.TUITION_AGGREGATION;
        if (resource == GraduateKnowledgeResource.PROGRAM && operation != GraduateKnowledgeOperation.NONE) return GraduateKnowledgeIntent.PROGRAM_LOOKUP;
        if ((resource == GraduateKnowledgeResource.CAMPUS || resource == GraduateKnowledgeResource.UNIVERSITY) && operation != GraduateKnowledgeOperation.NONE) return GraduateKnowledgeIntent.LOCATION_LOOKUP;
        if (resource == GraduateKnowledgeResource.FACULTY || resource == GraduateKnowledgeResource.DEPARTMENT) return GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP;
        return GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS;
    }

    private static boolean hasProgramScopedFilters(GraduateKnowledgeFilters filters) {
        return !filters.topicKeywords().isEmpty()
                || filters.programName() != null
                || !filters.languages().isEmpty()
                || !filters.admissionRequirementTypes().isEmpty();
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
                    || operation == GraduateKnowledgeOperation.EXISTS
                    || operation == GraduateKnowledgeOperation.COMPARE;
        }
        if (operation == GraduateKnowledgeOperation.OVERVIEW
                || operation == GraduateKnowledgeOperation.NONE) {
            return false;
        }
        return true;
    }

    public static boolean isScopeCompatible(GraduateKnowledgeResource resource, GraduateKnowledgeScope scope) {
        if (resource == null || scope == null) return false;
        return switch (resource) {
            case NONE -> scope == GraduateKnowledgeScope.GENERAL;
            case GRADUATE_OVERVIEW -> scope == GraduateKnowledgeScope.GRADUATE_OVERVIEW || scope == GraduateKnowledgeScope.UNIVERSITY;
            case UNIVERSITY -> scope == GraduateKnowledgeScope.UNIVERSITY;
            case CAMPUS -> scope == GraduateKnowledgeScope.CAMPUS || scope == GraduateKnowledgeScope.UNIVERSITY;
            case PROGRAM -> scope == GraduateKnowledgeScope.PROGRAM || scope == GraduateKnowledgeScope.UNIVERSITY;
            case FACULTY -> scope == GraduateKnowledgeScope.FACULTY || scope == GraduateKnowledgeScope.UNIVERSITY;
            case DEPARTMENT -> scope == GraduateKnowledgeScope.DEPARTMENT || scope == GraduateKnowledgeScope.FACULTY || scope == GraduateKnowledgeScope.UNIVERSITY;
        };
    }
}
