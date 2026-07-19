package com.uniai.chat.application.interpretation;

import com.uniai.chat.application.retrieval.GraduateKnowledgeOperation;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeResource;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.math.BigDecimal;

/** Validates the provider contract before catalog resolution or execution. */
public class CanonicalGraduateQueryDraftValidator {
    public static final int SCHEMA_VERSION = 2;
    private static final int MAX_UNIVERSITIES = 3;
    private static final int MAX_DEGREE_TYPES = 4;
    private static final int MAX_LANGUAGES = 4;
    private static final int MAX_ADMISSION_TYPES = 5;
    private static final int MAX_TOPICS = 5;
    private static final int MAX_UNSUPPORTED = 5;
    private static final int MAX_STRING_LENGTH = 120;
    private static final Set<String> SUPPORTED_BILLING_BASES = Set.of(
            "PER_CREDIT", "PER_SEMESTER", "PER_YEAR", "PER_TERM", "PER_PROGRAM",
            "FLAT_FEE", "PER_APPLICATION", "PER_ACADEMIC_YEAR"
    );
    private static final Set<String> SUPPORTED_TUITION_SCOPES = Set.of("UNIVERSITY", "FACULTY", "DEPARTMENT", "PROGRAM");

    public CanonicalGraduateQueryDraft validate(CanonicalGraduateQueryDraft draft) {
        if (draft == null) throw invalid("DRAFT_EMPTY");
        if (!Integer.valueOf(SCHEMA_VERSION).equals(draft.schemaVersion())) throw invalid("SCHEMA_VERSION_UNSUPPORTED");

        String resource = enumValue(draft.resource(), GraduateKnowledgeResource.class, "RESOURCE_INVALID");
        String operation = enumValue(draft.operation(), GraduateKnowledgeOperation.class, "OPERATION_INVALID");
        if (!GraduateKnowledgeQuery.isCompatible(GraduateKnowledgeResource.valueOf(resource), GraduateKnowledgeOperation.valueOf(operation))) {
            throw invalid("RESOURCE_OPERATION_INCOMPATIBLE");
        }
        validateFilters(draft.filters());
        validateAggregation(draft.aggregation(), resource, operation, draft.filters());
        validateSort(draft.sort());
        validateComparison(draft.comparison(), resource, operation);
        validateText(draft.detailLevel(), "DETAIL_LEVEL_TOO_LONG");
        if (draft.detailLevel() != null && !draft.detailLevel().isBlank()) {
            enumValue(draft.detailLevel(), Set.of("LIST", "DETAILS"), "DETAIL_LEVEL_UNSUPPORTED");
        }
        if (draft.limit() != null && (draft.limit() < 1 || draft.limit() > GraduateKnowledgeQuery.MAX_LIMIT)) {
            throw invalid("LIMIT_OUT_OF_RANGE");
        }
        validateList(draft.unsupportedConstraints(), MAX_UNSUPPORTED, "UNSUPPORTED_CONSTRAINTS_TOO_LARGE");
        return draft;
    }

    private void validateFilters(CanonicalGraduateQueryDraft.Filters filters) {
        if (filters == null) throw invalid("FILTERS_REQUIRED");
        validateList(filters.universities(), MAX_UNIVERSITIES, "UNIVERSITIES_TOO_LARGE");
        validateList(filters.degreeTypes(), MAX_DEGREE_TYPES, "DEGREE_TYPES_TOO_LARGE");
        validateList(filters.languages(), MAX_LANGUAGES, "LANGUAGES_TOO_LARGE");
        validateList(filters.admissionRequirementTypes(), MAX_ADMISSION_TYPES, "ADMISSION_TYPES_TOO_LARGE");
        validateList(filters.topicKeywords(), MAX_TOPICS, "TOPICS_TOO_LARGE");
        validateText(filters.city(), "CITY_TOO_LONG");
        validateText(filters.faculty(), "FACULTY_TOO_LONG");
        validateText(filters.department(), "DEPARTMENT_TOO_LONG");
        validateText(filters.programName(), "PROGRAM_NAME_TOO_LONG");
        if (filters.tuition() != null) {
            CanonicalGraduateQueryDraft.Tuition tuition = filters.tuition();
            validateText(tuition.thresholdOperator(), "THRESHOLD_OPERATOR_TOO_LONG");
            validateText(tuition.thresholdValue(), "THRESHOLD_VALUE_TOO_LONG");
            validateText(tuition.currency(), "CURRENCY_TOO_LONG");
            validateText(tuition.billingBasis(), "BILLING_BASIS_TOO_LONG");
            validateText(tuition.academicYear(), "ACADEMIC_YEAR_TOO_LONG");
            validateText(tuition.scopeLevel(), "TUITION_SCOPE_TOO_LONG");
            if (tuition.thresholdOperator() != null && !tuition.thresholdOperator().isBlank()) {
                enumValue(tuition.thresholdOperator(), Set.of("NONE", "LT", "LTE", "GT", "GTE"), "THRESHOLD_OPERATOR_UNSUPPORTED");
            }
            if (tuition.thresholdValue() != null && !tuition.thresholdValue().isBlank()) {
                try {
                    if (new BigDecimal(tuition.thresholdValue().trim()).signum() < 0) {
                        throw invalid("THRESHOLD_VALUE_NEGATIVE");
                    }
                } catch (NumberFormatException ex) {
                    throw invalid("THRESHOLD_VALUE_INVALID");
                }
            }
            if (tuition.billingBasis() != null && !tuition.billingBasis().isBlank()
                    && !SUPPORTED_BILLING_BASES.contains(tuition.billingBasis().trim().toUpperCase(Locale.ROOT))) {
                throw invalid("BILLING_BASIS_UNSUPPORTED");
            }
            if (tuition.scopeLevel() != null && !tuition.scopeLevel().isBlank()
                    && !SUPPORTED_TUITION_SCOPES.contains(tuition.scopeLevel().trim().toUpperCase(Locale.ROOT))) {
                throw invalid("TUITION_SCOPE_UNSUPPORTED");
            }
        }
    }

    private void validateAggregation(CanonicalGraduateQueryDraft.Aggregation aggregation, String resource, String operation,
                                     CanonicalGraduateQueryDraft.Filters filters) {
        if (!"AGGREGATE".equals(operation) && aggregation != null) throw invalid("AGGREGATION_UNEXPECTED");
        if ("AGGREGATE".equals(operation)) {
            if (aggregation == null || aggregation.function() == null || aggregation.function().isBlank()) {
                throw invalid("AGGREGATION_REQUIRED");
            }
            String function = aggregation.function().trim().toUpperCase(Locale.ROOT);
            if (!Set.of("AVG", "MIN", "MAX", "RANGE").contains(function)) throw invalid("AGGREGATION_FUNCTION_UNSUPPORTED");
            if (aggregation.field() == null || aggregation.field().isBlank()) throw invalid("AGGREGATION_FIELD_REQUIRED");
            validateText(aggregation.field(), "AGGREGATION_FIELD_TOO_LONG");
            if (!"TUITION".equals(aggregation.field().trim().toUpperCase(Locale.ROOT))) {
                throw invalid("AGGREGATION_FIELD_UNSUPPORTED");
            }
            if (!GraduateKnowledgeResource.PROGRAM.name().equals(resource)) throw invalid("AGGREGATION_RESOURCE_UNSUPPORTED");
            if ("RANGE".equals(function) && filters != null && filters.tuition() != null
                    && filters.tuition().thresholdOperator() != null && !filters.tuition().thresholdOperator().isBlank()) {
                throw invalid("RANGE_THRESHOLD_INCOMPATIBLE");
            }
        }
    }

    private void validateSort(CanonicalGraduateQueryDraft.Sort sort) {
        if (sort == null) return;
        enumValue(sort.field(), Set.of("TUITION", "NAME", "UNIVERSITY", "DEGREE_TYPE"), "SORT_FIELD_UNSUPPORTED");
        if (sort.direction() != null && !sort.direction().isBlank()) {
            enumValue(sort.direction(), Set.of("ASC", "DESC"), "SORT_DIRECTION_UNSUPPORTED");
        }
    }

    private void validateComparison(CanonicalGraduateQueryDraft.Comparison comparison, String resource, String operation) {
        if (comparison == null) {
            if ("COMPARE".equals(operation)) throw invalid("COMPARISON_REQUIRED");
            return;
        }
        if (!"COMPARE".equals(operation)) throw invalid("COMPARISON_UNEXPECTED");
        String dimension = enumValue(comparison.dimension(), Set.of(
                "UNIVERSITY", "CAMPUS_COUNT", "PROGRAM_AVAILABILITY", "PROGRAM_COUNT", "FACULTY_COUNT",
                "DEPARTMENT_COUNT", "LANGUAGE_AVAILABILITY", "ADMISSION_REQUIREMENTS", "TUITION_AVERAGE",
                "TUITION_MINIMUM", "TUITION_MAXIMUM", "TUITION_RANGE"), "COMPARISON_DIMENSION_UNSUPPORTED");
        boolean compatible = switch (dimension) {
            case "CAMPUS_COUNT" -> "CAMPUS".equals(resource);
            case "FACULTY_COUNT" -> "FACULTY".equals(resource);
            case "DEPARTMENT_COUNT" -> "DEPARTMENT".equals(resource);
            case "UNIVERSITY" -> Set.of("UNIVERSITY", "PROGRAM", "GRADUATE_OVERVIEW").contains(resource);
            default -> "PROGRAM".equals(resource);
        };
        if (!compatible) throw invalid("COMPARISON_RESOURCE_INCOMPATIBLE");
    }

    private void validateList(List<String> values, int max, String failure) {
        if (values == null || values.size() > max) throw invalid(failure);
        Set<String> seen = new HashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank() || value.length() > MAX_STRING_LENGTH || !seen.add(value.trim().toLowerCase(Locale.ROOT))) {
                if (value == null || value.isBlank() || value.length() > MAX_STRING_LENGTH) throw invalid(failure);
            }
        }
    }

    private void validateText(String value, String failure) {
        if (value != null && (value.length() > MAX_STRING_LENGTH || value.contains("\n") || value.contains("\r"))) throw invalid(failure);
    }

    private String enumValue(String value, Class<? extends Enum<?>> type, String failure) {
        if (value == null || value.isBlank()) throw invalid(failure);
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (Enum<?> constant : type.getEnumConstants()) {
            if (constant.name().equals(normalized)) return normalized;
        }
        throw invalid(failure);
    }

    private String enumValue(String value, Set<String> allowed, String failure) {
        if (value == null || value.isBlank() || !allowed.contains(value.trim().toUpperCase(Locale.ROOT))) throw invalid(failure);
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private IllegalArgumentException invalid(String reason) {
        return new IllegalArgumentException("Invalid canonical graduate query draft: " + reason);
    }
}
