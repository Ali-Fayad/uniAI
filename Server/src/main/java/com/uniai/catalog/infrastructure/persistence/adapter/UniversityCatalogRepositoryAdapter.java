package com.uniai.catalog.infrastructure.persistence.adapter;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.domain.repository.UniversityCatalogRepository;
import com.uniai.catalog.infrastructure.persistence.repository.UniversityCatalogJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UniversityCatalogRepositoryAdapter implements UniversityCatalogRepository {

    private final UniversityCatalogJpaRepository jpaRepository;

    @Override
    public List<UniversityCatalog> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<UniversityCatalog> searchByName(String search) {
        return jpaRepository.findByNameContainingIgnoreCaseOrderByNameAsc(search);
    }
}
