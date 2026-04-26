package com.uniai.catalog.domain.repository;

import com.uniai.catalog.domain.model.SkillCatalog;
import java.util.List;
import java.util.Optional;

public interface SkillCatalogRepository {
    List<SkillCatalog> findAll();

    List<SkillCatalog> searchByName(String search);

    Optional<SkillCatalog> findByNameIgnoreCase(String name);

    SkillCatalog save(SkillCatalog skill);
}
