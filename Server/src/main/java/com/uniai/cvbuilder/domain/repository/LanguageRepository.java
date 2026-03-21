package com.uniai.cvbuilder.domain.repository;

import com.uniai.cvbuilder.domain.model.Language;

import java.util.List;
import java.util.Optional;

public interface LanguageRepository {

    Optional<Language> findById(Long id);

    List<Language> findByCvId(Long cvId);

    Language save(Language language);

    void delete(Language language);

    void deleteByCvId(Long cvId);
}
