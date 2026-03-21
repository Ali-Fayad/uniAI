package com.uniai.cvbuilder.infrastructure.persistence.repository;

import com.uniai.cvbuilder.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByCvId(Long cvId);

    void deleteByCvId(Long cvId);
}
