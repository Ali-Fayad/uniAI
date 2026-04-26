package com.uniai.catalog.application.service;

import com.uniai.catalog.application.dto.response.LanguageCatalogResponse;
import com.uniai.catalog.application.dto.response.PositionCatalogResponse;
import com.uniai.catalog.application.dto.response.SkillCatalogResponse;
import com.uniai.catalog.application.dto.response.UniversityCatalogResponse;
import com.uniai.catalog.domain.model.LanguageCatalog;
import com.uniai.catalog.domain.model.PositionCatalog;
import com.uniai.catalog.domain.model.SkillCatalog;
import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.domain.repository.LanguageCatalogRepository;
import com.uniai.catalog.domain.repository.PositionCatalogRepository;
import com.uniai.catalog.domain.repository.SkillCatalogRepository;
import com.uniai.catalog.domain.repository.UniversityCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogQueryService {

    private final SkillCatalogRepository skillRepository;
    private final LanguageCatalogRepository languageRepository;
    private final PositionCatalogRepository positionRepository;
    private final UniversityCatalogRepository universityRepository;

    @Cacheable(value = "catalog-skills", key = "#search == null ? '' : #search.toLowerCase()")
    public List<SkillCatalogResponse> getSkills(String search) {
        List<SkillCatalog> rows = (search == null || search.isBlank())
                ? skillRepository.findAll().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toList()
                : skillRepository.searchByName(search);

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

    @Cacheable(value = "catalog-positions", key = "#search == null ? '' : #search.toLowerCase()")
    public List<PositionCatalogResponse> getPositions(String search) {
        List<PositionCatalog> rows = (search == null || search.isBlank())
                ? positionRepository.findAll().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toList()
                : positionRepository.searchByName(search);

        return rows.stream()
                .map(item -> new PositionCatalogResponse(item.getId(), item.getName()))
                .toList();
    }

    @Cacheable(value = "catalog-universities", key = "#search == null ? '' : #search.toLowerCase()")
    public List<UniversityCatalogResponse> getUniversities(String search) {
        List<UniversityCatalog> rows = (search == null || search.isBlank())
                ? universityRepository.findAll().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toList()
                : universityRepository.searchByName(search);

        return rows.stream()
                .map(item -> new UniversityCatalogResponse(item.getId(), item.getName(), item.getAcronym(), item.getNameAr()))
                .toList();
    }
}
