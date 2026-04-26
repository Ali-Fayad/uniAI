package com.uniai.catalog.domain.repository;

import com.uniai.catalog.domain.model.PositionCatalog;
import java.util.List;
import java.util.Optional;

public interface PositionCatalogRepository {
    List<PositionCatalog> findAll();

    List<PositionCatalog> searchByName(String search);

    Optional<PositionCatalog> findByNameIgnoreCase(String name);

    PositionCatalog save(PositionCatalog position);
}
