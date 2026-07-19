package com.uniai.chat.application.retrieval;

import com.uniai.chat.application.interpretation.CanonicalGraduateQueryDraft;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/** Builds executable graduate queries from validated, canonical domain input. */
public class GraduateKnowledgeQueryFactory {
    public GraduateKnowledgeQuery create(
            CanonicalGraduateQueryDraft draft,
            List<ResolvedUniversity> resolvedUniversities,
            GraduateKnowledgeIntent compatibilityIntent,
            boolean followUpResolved
    ) {
        if (draft == null || draft.filters() == null) throw new IllegalArgumentException("Canonical draft is required");
        GraduateKnowledgeResource resource = resource(draft.resource());
        GraduateKnowledgeOperation operation = operation(draft.operation());
        GraduateKnowledgeIntent intent = compatibilityIntent == null || compatibilityIntent == GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS
                ? GraduateKnowledgeQuery.deriveIntent(resource, operation)
                : compatibilityIntent;
        List<String> degrees = normalizeList(draft.filters().degreeTypes());
        CanonicalGraduateQueryDraft.Tuition tuition = draft.filters().tuition();
        GraduateKnowledgeThresholdOperator thresholdOperator = parseThresholdOperator(tuition == null ? null : tuition.thresholdOperator());
        BigDecimal thresholdValue = parseThresholdValue(tuition == null ? null : tuition.thresholdValue(), thresholdOperator);
        GraduateKnowledgeFilters filters = new GraduateKnowledgeFilters(
                resolvedUniversities == null ? List.of() : resolvedUniversities,
                degrees,
                normalizeList(draft.filters().topicKeywords()),
                draft.filters().city(),
                draft.filters().faculty(),
                draft.filters().department(),
                normalizeList(draft.filters().languages()),
                normalizeList(draft.filters().admissionRequirementTypes()),
                draft.filters().programName(),
                normalizeUpper(tuition == null ? null : tuition.currency()),
                normalizeUpper(tuition == null ? null : tuition.billingBasis()),
                tuition == null ? null : tuition.academicYear(),
                normalizeUpper(tuition == null ? null : tuition.scopeLevel()),
                thresholdOperator,
                thresholdValue
        );
        GraduateKnowledgeAggregation aggregation = aggregation(draft.aggregation(), operation, intent);
        GraduateKnowledgeSort sort = sort(draft.sort());
        Integer limit = draft.limit();
        GraduateKnowledgeComparisonDimension comparisonDimension = comparisonDimension(draft.comparison());
        GraduateProgramDetailLevel detailLevel = detailLevel(draft.detailLevel(), intent);
        GraduateKnowledgeFollowUpContext followUpContext = operation == GraduateKnowledgeOperation.COMPARE
                ? new GraduateKnowledgeFollowUpContext(null, null, resource, operation, List.of(), comparisonDimension)
                : GraduateKnowledgeFollowUpContext.empty();

        return new GraduateKnowledgeQuery(
                intent, resource, operation, filters, aggregation, sort, limit, followUpContext,
                detailLevel, followUpResolved || draft.comparison() != null, false
        ).withDecisionMetadata(GraduateKnowledgeInterpretationSource.AI, GraduateKnowledgeAmbiguityReason.NONE, false);
    }

    private GraduateKnowledgeResource resource(String value) {
        return GraduateKnowledgeResource.valueOf(required(value, "resource"));
    }

    private GraduateKnowledgeOperation operation(String value) {
        return GraduateKnowledgeOperation.valueOf(required(value, "operation"));
    }

    private String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private GraduateKnowledgeAggregation aggregation(CanonicalGraduateQueryDraft.Aggregation value,
                                                     GraduateKnowledgeOperation operation,
                                                     GraduateKnowledgeIntent intent) {
        if (operation != GraduateKnowledgeOperation.AGGREGATE) return GraduateKnowledgeAggregation.empty();
        if (value == null || value.function() == null || value.function().isBlank()) {
            return new GraduateKnowledgeAggregation(GraduateKnowledgeAggregationFunction.AVG, "tuition");
        }
        GraduateKnowledgeAggregationFunction function = GraduateKnowledgeAggregationFunction.valueOf(value.function().trim().toUpperCase(Locale.ROOT));
        if (intent != GraduateKnowledgeIntent.TUITION_AGGREGATION && value.field() == null) {
            throw new IllegalArgumentException("Aggregation field is required");
        }
        return new GraduateKnowledgeAggregation(function, value.field() == null ? "tuition" : value.field().trim().toLowerCase(Locale.ROOT));
    }

    private GraduateKnowledgeSort sort(CanonicalGraduateQueryDraft.Sort value) {
        if (value == null || value.field() == null || value.field().isBlank()) return GraduateKnowledgeSort.empty();
        GraduateKnowledgeSortField field = GraduateKnowledgeSortField.valueOf(value.field().trim().toUpperCase(Locale.ROOT));
        GraduateKnowledgeSortDirection direction = value.direction() == null || value.direction().isBlank()
                ? GraduateKnowledgeSortDirection.ASC
                : GraduateKnowledgeSortDirection.valueOf(value.direction().trim().toUpperCase(Locale.ROOT));
        return new GraduateKnowledgeSort(field, direction);
    }

    private GraduateKnowledgeComparisonDimension comparisonDimension(CanonicalGraduateQueryDraft.Comparison value) {
        return value == null || value.dimension() == null || value.dimension().isBlank()
                ? null
                : GraduateKnowledgeComparisonDimension.valueOf(value.dimension().trim().toUpperCase(Locale.ROOT));
    }

    private GraduateProgramDetailLevel detailLevel(String value, GraduateKnowledgeIntent intent) {
        if (intent != GraduateKnowledgeIntent.PROGRAM_LOOKUP) return null;
        return value == null || value.isBlank()
                ? GraduateProgramDetailLevel.LIST
                : GraduateProgramDetailLevel.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private GraduateKnowledgeThresholdOperator parseThresholdOperator(String value) {
        if (value == null || value.isBlank()) return GraduateKnowledgeThresholdOperator.NONE;
        return GraduateKnowledgeThresholdOperator.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private BigDecimal parseThresholdValue(String value, GraduateKnowledgeThresholdOperator operator) {
        if (operator == GraduateKnowledgeThresholdOperator.NONE) {
            if (value != null && !value.isBlank()) throw new IllegalArgumentException("Threshold value requires an operator");
            return null;
        }
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Threshold value is required");
        BigDecimal parsed = new BigDecimal(value.trim());
        if (parsed.signum() < 0) throw new IllegalArgumentException("Threshold must not be negative");
        return parsed;
    }

    private List<String> normalizeList(List<String> values) {
        if (values == null || values.isEmpty()) return List.of();
        return new ArrayList<>(new LinkedHashSet<>(values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList()));
    }

    private String normalizeUpper(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
