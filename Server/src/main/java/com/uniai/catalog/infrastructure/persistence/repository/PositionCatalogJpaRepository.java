package com.uniai.catalog.infrastructure.persistence.repository;

import com.uniai.catalog.domain.model.PositionCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionCatalogJpaRepository extends JpaRepository<PositionCatalog, Long> {
    Optional<PositionCatalog> findByNameIgnoreCase(String name);

    List<PositionCatalog> findByNameContainingIgnoreCaseOrderByNameAsc(String search);
}
