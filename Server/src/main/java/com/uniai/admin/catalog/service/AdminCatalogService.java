package com.uniai.admin.catalog.service;

import com.uniai.admin.catalog.dto.CatalogAdminResponse;
import com.uniai.admin.catalog.dto.CreatePositionRequest;
import com.uniai.admin.catalog.dto.CreateSkillRequest;
import com.uniai.catalog.domain.model.PositionCatalog;
import com.uniai.catalog.domain.model.SkillCatalog;
import com.uniai.catalog.domain.repository.PositionCatalogRepository;
import com.uniai.catalog.domain.repository.SkillCatalogRepository;
import com.uniai.shared.exception.AlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminCatalogService {

    private final SkillCatalogRepository skillRepository;
    private final PositionCatalogRepository positionRepository;

    public List<CatalogAdminResponse> searchSkills(String query, int limit) {
        String normalizedQuery = clean(query);
        return (normalizedQuery.isBlank()
                ? skillRepository.findAll()
                : skillRepository.searchByNameOrCategory(normalizedQuery))
                .stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .limit(safeLimit(limit))
                .map(skill -> new CatalogAdminResponse(skill.getId(), skill.getName(), skill.getCategory()))
                .toList();
    }

    public List<CatalogAdminResponse> searchPositions(String query, int limit) {
        String normalizedQuery = clean(query);
        return (normalizedQuery.isBlank()
                ? positionRepository.findAll()
                : positionRepository.searchByName(normalizedQuery))
                .stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .limit(safeLimit(limit))
                .map(position -> new CatalogAdminResponse(position.getId(), position.getName(), null))
                .toList();
    }

    @Transactional
    @CacheEvict(value = "catalog-skills", allEntries = true)
    public CatalogAdminResponse createSkill(CreateSkillRequest request) {
        String name = displayName(request.name());
        if (skillRepository.findAll().stream().anyMatch(skill -> normalizedKey(skill.getName()).equals(normalizedKey(name)))) {
            throw new AlreadyExistsException("Skill already exists");
        }
        try {
            SkillCatalog saved = skillRepository.save(SkillCatalog.builder()
                    .name(name)
                    .category(blankToNull(request.category()))
                    .build());
            return new CatalogAdminResponse(saved.getId(), saved.getName(), saved.getCategory());
        } catch (DataIntegrityViolationException exception) {
            throw new AlreadyExistsException("Skill already exists");
        }
    }

    @Transactional
    @CacheEvict(value = "catalog-positions", allEntries = true)
    public CatalogAdminResponse createPosition(CreatePositionRequest request) {
        String name = displayName(request.name());
        if (positionRepository.findAll().stream().anyMatch(position -> normalizedKey(position.getName()).equals(normalizedKey(name)))) {
            throw new AlreadyExistsException("Position already exists");
        }
        try {
            PositionCatalog saved = positionRepository.save(PositionCatalog.builder().name(name).build());
            return new CatalogAdminResponse(saved.getId(), saved.getName(), null);
        } catch (DataIntegrityViolationException exception) {
            throw new AlreadyExistsException("Position already exists");
        }
    }

    static String normalizedKey(String value) {
        return displayName(value).toLowerCase(Locale.ROOT);
    }

    private static String displayName(String value) {
        return clean(value).replaceAll("\\s+", " ");
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String blankToNull(String value) {
        String cleaned = clean(value);
        return cleaned.isBlank() ? null : cleaned;
    }

    private static int safeLimit(int limit) {
        return Math.max(1, Math.min(limit <= 0 ? 50 : limit, 100));
    }
}
