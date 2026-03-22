package com.uniai.cvbuilder.infrastructure.persistence.adapter;

import com.uniai.cvbuilder.domain.model.Project;
import com.uniai.cvbuilder.domain.repository.ProjectRepository;
import com.uniai.cvbuilder.infrastructure.persistence.repository.JpaProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-backed implementation of {@link ProjectRepository}.
 */
@Repository
@RequiredArgsConstructor
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final JpaProjectRepository jpaRepository;

    @Override
    public Optional<Project> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Project> findByCvId(Long cvId) {
        return jpaRepository.findByCvId(cvId);
    }

    @Override
    public Project save(Project project) {
        return jpaRepository.save(project);
    }

    @Override
    public void delete(Project project) {
        jpaRepository.delete(project);
    }

    @Override
    public void deleteByCvId(Long cvId) {
        jpaRepository.deleteByCvId(cvId);
    }
}
