package com.uniai.catalog.infrastructure.persistence.adapter;

import com.uniai.catalog.domain.model.SkillCatalog;
import com.uniai.catalog.domain.repository.SkillCatalogRepository;
import com.uniai.catalog.infrastructure.persistence.repository.SkillCatalogJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SkillCatalogRepositoryAdapter implements SkillCatalogRepository {

    private final SkillCatalogJpaRepository jpaRepository;

    @Override
    public List<SkillCatalog> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<SkillCatalog> searchByName(String search) {
        return jpaRepository.findByNameContainingIgnoreCaseOrderByNameAsc(search);
    }

    @Override
    public Optional<SkillCatalog> findByNameIgnoreCase(String name) {
        return jpaRepository.findByNameIgnoreCase(name);
    }

    @Override
    public SkillCatalog save(SkillCatalog skill) {
        return jpaRepository.save(skill);
    }
}
