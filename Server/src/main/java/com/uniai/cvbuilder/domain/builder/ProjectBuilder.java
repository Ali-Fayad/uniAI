package com.uniai.cvbuilder.domain.builder;

import com.uniai.cvbuilder.domain.model.Project;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ProjectBuilder {

    private final Project.ProjectBuilder builder;

    private ProjectBuilder(Long cvId, String name) {
        this.builder = Project.builder()
                .cvId(cvId)
                .name(name)
                .technologies(new ArrayList<>());
    }

    public static ProjectBuilder newProject(Long cvId, String name) {
        return new ProjectBuilder(cvId, name);
    }

    public ProjectBuilder description(String description) {
        builder.description(description);
        return this;
    }

    public ProjectBuilder githubUrl(String githubUrl) {
        builder.githubUrl(githubUrl);
        return this;
    }

    public ProjectBuilder liveUrl(String liveUrl) {
        builder.liveUrl(liveUrl);
        return this;
    }

    public ProjectBuilder startDate(LocalDate startDate) {
        builder.startDate(startDate);
        return this;
    }

    public ProjectBuilder endDate(LocalDate endDate) {
        builder.endDate(endDate);
        return this;
    }

    public ProjectBuilder technologies(List<String> technologies) {
        builder.technologies(technologies != null ? technologies : new ArrayList<>());
        return this;
    }

    public Project build() {
        return builder.build();
    }
}
