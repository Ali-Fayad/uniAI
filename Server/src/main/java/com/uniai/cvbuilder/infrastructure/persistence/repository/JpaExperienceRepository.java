package com.uniai.cvbuilder.infrastructure.persistence.repository;

import com.uniai.cvbuilder.domain.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for professional experience entries linked to CVs.
 */
@Repository
public interface JpaExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByCvId(Long cvId);

    void deleteByCvId(Long cvId);
}
