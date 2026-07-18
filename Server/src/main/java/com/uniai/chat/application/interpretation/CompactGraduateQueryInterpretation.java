package com.uniai.chat.application.interpretation;

import java.util.List;

/** Compact provider-facing contract. Catalog resolution and validation remain in Java. */
public record CompactGraduateQueryInterpretation(
        Integer schemaVersion,
        String resource,
        String operation,
        List<String> universities,
        List<String> degreeTypes,
        String city,
        String faculty,
        String department,
        String programName,
        List<String> languages,
        List<String> admissionRequirementTypes,
        List<String> topicKeywords,
        String detailLevel,
        String aggregation,
        Tuition tuition,
        Sort sort,
        Integer limit,
        String comparisonDimension,
        Boolean comparison,
        Boolean ambiguous,
        String clarificationNeeded,
        List<String> unsupportedConstraints
) {
    public CompactGraduateQueryInterpretation {
        universities = universities == null ? List.of() : List.copyOf(universities);
        degreeTypes = degreeTypes == null ? List.of() : List.copyOf(degreeTypes);
        languages = languages == null ? List.of() : List.copyOf(languages);
        admissionRequirementTypes = admissionRequirementTypes == null ? List.of() : List.copyOf(admissionRequirementTypes);
        topicKeywords = topicKeywords == null ? List.of() : List.copyOf(topicKeywords);
        unsupportedConstraints = unsupportedConstraints == null ? List.of() : List.copyOf(unsupportedConstraints);
    }

    public record Tuition(
            String thresholdOperator,
            String thresholdValue,
            String currency,
            String billingBasis,
            String academicYear,
            String scopeLevel
    ) {}

    public record Sort(String field, String direction) {}

    public GraduateQueryInterpretation toLegacyInterpretation() {
        return new GraduateQueryInterpretation(
                schemaVersion,
                null,
                universities,
                degreeTypes,
                detailLevel,
                false,
                comparison,
                topicKeywords,
                ambiguous,
                clarificationNeeded,
                unsupportedConstraints,
                resource,
                operation,
                city,
                faculty,
                department,
                languages,
                admissionRequirementTypes,
                programName,
                aggregation,
                tuition == null ? null : tuition.thresholdOperator(),
                tuition == null ? null : tuition.thresholdValue(),
                tuition == null ? null : tuition.currency(),
                tuition == null ? null : tuition.billingBasis(),
                tuition == null ? null : tuition.academicYear(),
                tuition == null ? null : tuition.scopeLevel(),
                sort == null ? null : sort.field(),
                sort == null ? null : sort.direction(),
                limit,
                comparisonDimension
        );
    }
}
