package com.uniai.catalog.infrastructure.persistence.repository;

import com.uniai.catalog.domain.model.SkillCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillCatalogJpaRepository extends JpaRepository<SkillCatalog, Long> {
    Optional<SkillCatalog> findByNameIgnoreCase(String name);

    List<SkillCatalog> findByNameContainingIgnoreCaseOrderByNameAsc(String search);
}
