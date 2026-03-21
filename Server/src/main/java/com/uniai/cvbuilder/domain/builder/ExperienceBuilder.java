package com.uniai.cvbuilder.domain.builder;

import com.uniai.cvbuilder.domain.model.Experience;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ExperienceBuilder {

    private final Experience.ExperienceBuilder builder;

    private ExperienceBuilder(Long cvId, String position, String company, LocalDate startDate) {
        this.builder = Experience.builder()
                .cvId(cvId)
                .position(position)
                .company(company)
                .startDate(startDate)
                .isCurrent(false)
                .achievements(new ArrayList<>());
    }

    public static ExperienceBuilder newExperience(Long cvId, String position, String company, LocalDate startDate) {
        return new ExperienceBuilder(cvId, position, company, startDate);
    }

    public ExperienceBuilder location(String location) {
        builder.location(location);
        return this;
    }

    public ExperienceBuilder endDate(LocalDate endDate) {
        builder.endDate(endDate);
        return this;
    }

    public ExperienceBuilder current(boolean isCurrent) {
        builder.isCurrent(isCurrent);
        return this;
    }

    public ExperienceBuilder description(String description) {
        builder.description(description);
        return this;
    }

    public ExperienceBuilder achievements(List<String> achievements) {
        builder.achievements(achievements != null ? achievements : new ArrayList<>());
        return this;
    }

    public Experience build() {
        return builder.build();
    }
}
