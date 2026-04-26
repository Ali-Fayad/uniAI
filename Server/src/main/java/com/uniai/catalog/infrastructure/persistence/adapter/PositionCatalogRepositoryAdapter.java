package com.uniai.catalog.infrastructure.persistence.adapter;

import com.uniai.catalog.domain.model.PositionCatalog;
import com.uniai.catalog.domain.repository.PositionCatalogRepository;
import com.uniai.catalog.infrastructure.persistence.repository.PositionCatalogJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PositionCatalogRepositoryAdapter implements PositionCatalogRepository {

    private final PositionCatalogJpaRepository jpaRepository;

    @Override
    public List<PositionCatalog> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<PositionCatalog> searchByName(String search) {
        return jpaRepository.findByNameContainingIgnoreCaseOrderByNameAsc(search);
    }

    @Override
    public Optional<PositionCatalog> findByNameIgnoreCase(String name) {
        return jpaRepository.findByNameIgnoreCase(name);
    }

    @Override
    public PositionCatalog save(PositionCatalog position) {
        return jpaRepository.save(position);
    }
}
