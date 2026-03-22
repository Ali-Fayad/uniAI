package com.uniai.cvbuilder.domain.repository;

import com.uniai.cvbuilder.domain.model.Project;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository abstraction for project entries linked to CVs.
 */
public interface ProjectRepository {

    Optional<Project> findById(Long id);

    List<Project> findByCvId(Long cvId);

    Project save(Project project);

    void delete(Project project);

    void deleteByCvId(Long cvId);
}
