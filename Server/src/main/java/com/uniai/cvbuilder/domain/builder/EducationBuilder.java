package com.uniai.cvbuilder.domain.builder;

import com.uniai.cvbuilder.domain.model.Education;

import java.time.LocalDate;

/**
 * Fluent builder for assembling {@link Education} aggregates with university linkage and timing.
 */
public final class EducationBuilder {

    private final Education.EducationBuilder builder;

    private EducationBuilder(Long cvId, String degree, String fieldOfStudy, LocalDate startDate) {
        this.builder = Education.builder()
                .cvId(cvId)
                .degree(degree)
                .fieldOfStudy(fieldOfStudy)
                .startDate(startDate);
    }

    public static EducationBuilder newEducation(Long cvId, String degree, String fieldOfStudy, LocalDate startDate) {
        return new EducationBuilder(cvId, degree, fieldOfStudy, startDate);
    }

    public EducationBuilder universityId(Long universityId) {
        builder.universityId(universityId);
        return this;
    }

    public EducationBuilder endDate(LocalDate endDate) {
        builder.endDate(endDate);
        return this;
    }

    public EducationBuilder grade(String grade) {
        builder.grade(grade);
        return this;
    }

    public EducationBuilder description(String description) {
        builder.description(description);
        return this;
    }

    public Education build() {
        return builder.build();
    }
}
