package com.uniai.chat.application.retrieval;

import java.util.List;
import java.math.BigDecimal;

/**
 * Immutable filter boundary for graduate knowledge queries.
 *
 * Only currently supported filters are populated. Future retrieval tasks can
 * add typed dimensions here without introducing an unbounded map contract.
 */
public record GraduateKnowledgeFilters(
        List<ResolvedUniversity> universities,
        List<String> degreeTypes,
        List<String> topicKeywords,
        String city,
        String facultyName,
        String departmentName,
        List<String> languages,
        List<String> admissionRequirementTypes,
        String programName,
        String currency,
        String billingBasis,
        String academicYear,
        String tuitionScopeLevel,
        GraduateKnowledgeThresholdOperator thresholdOperator,
        BigDecimal thresholdValue
) {
    public GraduateKnowledgeFilters(
            List<ResolvedUniversity> universities,
            List<String> degreeTypes,
            List<String> topicKeywords
    ) {
        this(universities, degreeTypes, topicKeywords, null, null, null, List.of(), List.of(), null,
                null, null, null, null, GraduateKnowledgeThresholdOperator.NONE, null);
    }

    public GraduateKnowledgeFilters(
            List<ResolvedUniversity> universities,
            List<String> degreeTypes,
            List<String> topicKeywords,
            String city
    ) {
        this(universities, degreeTypes, topicKeywords, city, null, null, List.of(), List.of(), null,
                null, null, null, null, GraduateKnowledgeThresholdOperator.NONE, null);
    }

    public GraduateKnowledgeFilters(
            List<ResolvedUniversity> universities,
            List<String> degreeTypes,
            List<String> topicKeywords,
            String city,
            String facultyName,
            String departmentName
    ) {
        this(universities, degreeTypes, topicKeywords, city, facultyName, departmentName, List.of(), List.of(), null,
                null, null, null, null, GraduateKnowledgeThresholdOperator.NONE, null);
    }

    public GraduateKnowledgeFilters(
            List<ResolvedUniversity> universities,
            List<String> degreeTypes,
            List<String> topicKeywords,
            String city,
            String facultyName,
            String departmentName,
            List<String> languages,
            List<String> admissionRequirementTypes,
            String programName
    ) {
        this(universities, degreeTypes, topicKeywords, city, facultyName, departmentName,
                languages, admissionRequirementTypes, programName, null, null, null, null,
                GraduateKnowledgeThresholdOperator.NONE, null);
    }

    public GraduateKnowledgeFilters {
        universities = universities == null ? List.of() : List.copyOf(universities);
        degreeTypes = degreeTypes == null ? List.of() : List.copyOf(degreeTypes);
        topicKeywords = topicKeywords == null ? List.of() : List.copyOf(topicKeywords);
        city = city == null || city.isBlank() ? null : city.trim();
        facultyName = facultyName == null || facultyName.isBlank() ? null : facultyName.trim();
        departmentName = departmentName == null || departmentName.isBlank() ? null : departmentName.trim();
        languages = languages == null ? List.of() : List.copyOf(languages);
        admissionRequirementTypes = admissionRequirementTypes == null ? List.of() : List.copyOf(admissionRequirementTypes);
        programName = programName == null || programName.isBlank() ? null : programName.trim();
        currency = currency == null || currency.isBlank() ? null : currency.trim().toUpperCase();
        billingBasis = billingBasis == null || billingBasis.isBlank() ? null : billingBasis.trim().toUpperCase();
        academicYear = academicYear == null || academicYear.isBlank() ? null : academicYear.trim();
        tuitionScopeLevel = tuitionScopeLevel == null || tuitionScopeLevel.isBlank() ? null : tuitionScopeLevel.trim().toUpperCase();
        thresholdOperator = thresholdOperator == null ? GraduateKnowledgeThresholdOperator.NONE : thresholdOperator;
        if (thresholdOperator == GraduateKnowledgeThresholdOperator.NONE) {
            thresholdValue = null;
        }
        if (thresholdValue != null && thresholdValue.signum() < 0) {
            throw new IllegalArgumentException("Tuition threshold must not be negative");
        }
    }

    public static GraduateKnowledgeFilters empty() {
        return new GraduateKnowledgeFilters(List.of(), List.of(), List.of(), null, null, null, List.of(), List.of(), null,
                null, null, null, null, GraduateKnowledgeThresholdOperator.NONE, null);
    }
}
