package com.uniai.catalog.application.service;

import com.uniai.catalog.application.dto.response.LanguageCatalogResponse;
import com.uniai.catalog.application.dto.response.SkillCatalogResponse;
import com.uniai.catalog.domain.model.LanguageCatalog;
import com.uniai.catalog.domain.model.SkillCatalog;
import com.uniai.catalog.infrastructure.persistence.repository.LanguageCatalogJpaRepository;
import com.uniai.catalog.infrastructure.persistence.repository.SkillCatalogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogQueryService {

    private final SkillCatalogJpaRepository skillRepository;
    private final LanguageCatalogJpaRepository languageRepository;

    @Cacheable(value = "catalog-skills", key = "#search == null ? '' : #search.toLowerCase()")
    public List<SkillCatalogResponse> getSkills(String search) {
        List<SkillCatalog> rows = (search == null || search.isBlank())
                ? skillRepository.findAll().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toList()
                : skillRepository.findByNameContainingIgnoreCaseOrderByNameAsc(search);

        return rows.stream()
                .map(item -> new SkillCatalogResponse(item.getId(), item.getName(), item.getCategory()))
                .toList();
    }

    @Cacheable(value = "catalog-languages", key = "#search == null ? '' : #search.toLowerCase()")
    public List<LanguageCatalogResponse> getLanguages(String search) {
        List<LanguageCatalog> rows = (search == null || search.isBlank())
                ? languageRepository.findAll().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toList()
                : languageRepository.searchByNameOrNativeName(search);

        return rows.stream()
                .map(item -> new LanguageCatalogResponse(item.getId(), item.getName(), item.getCode(), item.getNativeName()))
                .toList();
    }
}
