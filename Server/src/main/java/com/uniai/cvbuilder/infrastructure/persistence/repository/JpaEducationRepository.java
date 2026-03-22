package com.uniai.cvbuilder.infrastructure.persistence.repository;

import com.uniai.cvbuilder.domain.model.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for education entries linked to CVs.
 */
@Repository
public interface JpaEducationRepository extends JpaRepository<Education, Long> {

    List<Education> findByCvId(Long cvId);

    void deleteByCvId(Long cvId);
}
