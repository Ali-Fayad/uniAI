package com.uniai.catalog.infrastructure.persistence.adapter;

import com.uniai.catalog.domain.model.LanguageCatalog;
import com.uniai.catalog.domain.repository.LanguageCatalogRepository;
import com.uniai.catalog.infrastructure.persistence.repository.LanguageCatalogJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LanguageCatalogRepositoryAdapter implements LanguageCatalogRepository {

    private final LanguageCatalogJpaRepository jpaRepository;

    @Override
    public List<LanguageCatalog> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<LanguageCatalog> searchByNameOrNativeName(String search) {
        return jpaRepository.searchByNameOrNativeName(search);
    }
}
