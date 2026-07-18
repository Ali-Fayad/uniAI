package com.uniai.catalog.infrastructure.persistence.repository;

import com.uniai.catalog.domain.model.CampusCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampusCatalogJpaRepository extends JpaRepository<CampusCatalog, Long> {
}
