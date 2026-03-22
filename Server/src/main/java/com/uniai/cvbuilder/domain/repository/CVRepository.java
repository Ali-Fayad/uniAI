package com.uniai.cvbuilder.domain.repository;

import com.uniai.cvbuilder.domain.model.CV;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository abstraction for persisting and retrieving CV aggregates and defaults.
 */
public interface CVRepository {

    Optional<CV> findById(Long id);

    List<CV> findByUserId(Long userId);

    Optional<CV> findDefaultByUserId(Long userId);

    CV save(CV cv);

    void delete(CV cv);

    void deleteById(Long id);
}
