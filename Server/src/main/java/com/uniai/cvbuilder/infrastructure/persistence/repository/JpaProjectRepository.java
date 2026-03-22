package com.uniai.cvbuilder.infrastructure.persistence.repository;

import com.uniai.cvbuilder.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for project entries linked to CVs.
 */
@Repository
public interface JpaProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByCvId(Long cvId);

    void deleteByCvId(Long cvId);
}
