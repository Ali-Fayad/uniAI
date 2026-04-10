package com.uniai.cvbuilder.domain.repository;

import com.uniai.cvbuilder.domain.model.CVTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository abstraction for CV template retrieval.
 */
public interface CVTemplateRepository {

    List<CVTemplate> findAllActive();

    Optional<CVTemplate> findById(Long id);

    Optional<CVTemplate> findActiveByComponentName(String componentName);
}
