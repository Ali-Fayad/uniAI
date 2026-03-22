package com.uniai.cvbuilder.domain.repository;

import com.uniai.cvbuilder.domain.model.Education;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository abstraction for education section entries belonging to CVs.
 */
public interface EducationRepository {

    Optional<Education> findById(Long id);

    List<Education> findByCvId(Long cvId);

    Education save(Education education);

    void delete(Education education);

    void deleteByCvId(Long cvId);
}
