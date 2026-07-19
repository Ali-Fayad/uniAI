package com.uniai.chat.application.interpretation;

import com.uniai.chat.application.retrieval.GraduateKnowledgeOperation;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeResource;

import java.util.List;
import java.util.Locale;

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
                deriveCompatibilityIntent(resource, operation),
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

    /**
     * Task 1 compatibility bridge: providers emit compact typed routing while
     * the existing validator still requires the legacy intent field.
     *
     * The value remains deterministic and is derived only from supported
     * domain routing values; malformed routing intentionally remains null so
     * the existing validator reports an invalid provider contract.
     */
    private static String deriveCompatibilityIntent(String resourceValue, String operationValue) {
        if (resourceValue == null || resourceValue.isBlank()
                || operationValue == null || operationValue.isBlank()) {
            return null;
        }

        try {
            GraduateKnowledgeResource resource = GraduateKnowledgeResource.valueOf(resourceValue.trim().toUpperCase(Locale.ROOT));
            GraduateKnowledgeOperation operation = GraduateKnowledgeOperation.valueOf(operationValue.trim().toUpperCase(Locale.ROOT));
            if (!GraduateKnowledgeQuery.isCompatible(resource, operation)) {
                return null;
            }
            return GraduateKnowledgeQuery.deriveIntent(resource, operation).name();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
