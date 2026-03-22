package com.uniai.cvbuilder.infrastructure.persistence.adapter;

import com.uniai.cvbuilder.domain.model.Language;
import com.uniai.cvbuilder.domain.repository.LanguageRepository;
import com.uniai.cvbuilder.infrastructure.persistence.repository.JpaLanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-backed implementation of {@link LanguageRepository}.
 */
@Repository
@RequiredArgsConstructor
public class LanguageRepositoryAdapter implements LanguageRepository {

    private final JpaLanguageRepository jpaRepository;

    @Override
    public Optional<Language> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Language> findByCvId(Long cvId) {
        return jpaRepository.findByCvId(cvId);
    }

    @Override
    public Language save(Language language) {
        return jpaRepository.save(language);
    }

    @Override
    public void delete(Language language) {
        jpaRepository.delete(language);
    }

    @Override
    public void deleteByCvId(Long cvId) {
        jpaRepository.deleteByCvId(cvId);
    }
}
