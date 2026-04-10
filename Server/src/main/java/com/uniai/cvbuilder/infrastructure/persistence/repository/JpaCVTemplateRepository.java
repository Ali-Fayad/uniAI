package com.uniai.cvbuilder.infrastructure.persistence.repository;

import com.uniai.cvbuilder.domain.model.CVTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for CV templates.
 */
@Repository
public interface JpaCVTemplateRepository extends JpaRepository<CVTemplate, Long> {

    List<CVTemplate> findByIsActiveTrueOrderByNameAsc();

    Optional<CVTemplate> findByComponentNameAndIsActiveTrue(String componentName);
}
