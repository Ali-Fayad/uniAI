package com.uniai.cvbuilder.domain.repository;

import com.uniai.cvbuilder.domain.model.University;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository abstraction for university reference data used by education entries.
 */
public interface UniversityRepository {

    Optional<University> findById(Long id);

    List<University> findAll();

    University save(University university);
}
