package com.uniai.catalog.infrastructure.persistence.repository;

import com.uniai.catalog.domain.model.UniversityCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface UniversityCatalogJpaRepository extends JpaRepository<UniversityCatalog, Long> {

    @EntityGraph(attributePaths = "campuses")
    List<UniversityCatalog> findByNameContainingIgnoreCaseOrAcronymContainingIgnoreCaseOrNameArContainingIgnoreCaseOrderByNameAsc(
            String name,
            String acronym,
            String nameAr
    );

    @Override
    @EntityGraph(attributePaths = "campuses")
    List<UniversityCatalog> findAll();
}
