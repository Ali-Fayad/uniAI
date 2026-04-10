package com.uniai.cvbuilder.infrastructure.persistence.adapter;

import com.uniai.cvbuilder.domain.model.CVTemplate;
import com.uniai.cvbuilder.domain.repository.CVTemplateRepository;
import com.uniai.cvbuilder.infrastructure.persistence.repository.JpaCVTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-backed implementation of {@link CVTemplateRepository}.
 */
@Repository
@RequiredArgsConstructor
public class CVTemplateRepositoryAdapter implements CVTemplateRepository {

    private final JpaCVTemplateRepository jpaRepository;

    @Override
    public List<CVTemplate> findAllActive() {
        return jpaRepository.findByIsActiveTrueOrderByNameAsc();
    }

    @Override
    public Optional<CVTemplate> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<CVTemplate> findActiveByComponentName(String componentName) {
        return jpaRepository.findByComponentNameAndIsActiveTrue(componentName);
    }
}
