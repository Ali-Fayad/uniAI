package com.uniai.catalog.infrastructure.persistence.repository;

import com.uniai.catalog.domain.model.UniversityCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UniversityCatalogJpaRepository extends JpaRepository<UniversityCatalog, Long> {

    List<UniversityCatalog> findByNameContainingIgnoreCaseOrderByNameAsc(String search);
}
