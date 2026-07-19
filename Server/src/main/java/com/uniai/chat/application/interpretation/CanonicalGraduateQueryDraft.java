package com.uniai.chat.application.interpretation;

import java.util.List;

/**
 * Provider-facing graduate-query contract.
 *
 * This model intentionally contains user-facing domain values only. Catalog
 * resolution and conversion to the executable GraduateKnowledgeQuery remain
 * server-owned compatibility steps.
 */
public record CanonicalGraduateQueryDraft(
        Integer schemaVersion,
        String resource,
        String operation,
        Filters filters,
        Aggregation aggregation,
        Sort sort,
        Comparison comparison,
        String detailLevel,
        Integer limit,
        Boolean clarificationRequired,
        List<String> unsupportedConstraints
) {
    public CanonicalGraduateQueryDraft {
        unsupportedConstraints = unsupportedConstraints == null ? List.of() : List.copyOf(unsupportedConstraints);
        clarificationRequired = Boolean.TRUE.equals(clarificationRequired);
    }

    public record Filters(
            List<String> universities,
            List<String> degreeTypes,
            String city,
            String faculty,
            String department,
            String programName,
            List<String> languages,
            List<String> admissionRequirementTypes,
            List<String> topicKeywords,
            Tuition tuition
    ) {
        public Filters {
            universities = universities == null ? List.of() : List.copyOf(universities);
            degreeTypes = degreeTypes == null ? List.of() : List.copyOf(degreeTypes);
            languages = languages == null ? List.of() : List.copyOf(languages);
            admissionRequirementTypes = admissionRequirementTypes == null ? List.of() : List.copyOf(admissionRequirementTypes);
            topicKeywords = topicKeywords == null ? List.of() : List.copyOf(topicKeywords);
        }

        public static Filters empty() {
            return new Filters(List.of(), List.of(), null, null, null, null, List.of(), List.of(), List.of(), null);
        }
    }

    public record Tuition(
            String thresholdOperator,
            String thresholdValue,
            String currency,
            String billingBasis,
            String academicYear,
            String scopeLevel
    ) {}

    public record Aggregation(String function, String field) {}

    public record Sort(String field, String direction) {}

    public record Comparison(String dimension) {}
}
