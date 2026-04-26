package com.uniai.catalog.application.service;

import com.uniai.catalog.domain.model.PositionCatalog;
import com.uniai.catalog.domain.model.SkillCatalog;
import com.uniai.catalog.domain.repository.PositionCatalogRepository;
import com.uniai.catalog.domain.repository.SkillCatalogRepository;
import com.uniai.cvbuilder.infrastructure.client.PositionsApiClient;
import com.uniai.cvbuilder.infrastructure.client.SkillsApiClient;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogSyncService {

    private static final Logger logger = LogManager.getLogger(CatalogSyncService.class);

    private final SkillsApiClient skillsApiClient;
    private final PositionsApiClient positionsApiClient;
    private final SkillCatalogRepository skillRepository;
    private final PositionCatalogRepository positionRepository;

    @CacheEvict(value = {"catalog-skills", "catalog-languages", "catalog-positions", "catalog-universities"}, allEntries = true)
    public void syncExternalCatalogs() {
        syncSkills();
        syncPositions();
    }

    public void syncSkills() {
        try {
            List<SkillsApiClient.SkillItem> externalSkills = skillsApiClient.fetchSkillItems();
            for (SkillsApiClient.SkillItem externalSkill : externalSkills) {
                String normalizedName = externalSkill.name().trim();
                if (normalizedName.isEmpty()) {
                    continue;
                }

                SkillCatalog row = skillRepository.findByNameIgnoreCase(normalizedName)
                        .orElseGet(() -> SkillCatalog.builder().name(normalizedName).build());
                row.setCategory(externalSkill.category());
                skillRepository.save(row);
            }
            logger.info("Skills sync completed with {} records", externalSkills.size());
        } catch (Exception ex) {
            logger.warn("Skills sync failed. Serving cached DB data. Cause: {}", ex.getMessage());
        }
    }

    public void syncPositions() {
        try {
            List<String> externalPositions = positionsApiClient.fetchPositions();
            for (String externalPosition : externalPositions) {
                String normalizedName = externalPosition == null ? "" : externalPosition.trim();
                if (normalizedName.isEmpty()) {
                    continue;
                }

                PositionCatalog row = positionRepository.findByNameIgnoreCase(normalizedName)
                        .orElseGet(() -> PositionCatalog.builder().name(normalizedName).build());
                positionRepository.save(row);
            }
            logger.info("Positions sync completed with {} records", externalPositions.size());
        } catch (Exception ex) {
            logger.warn("Positions sync failed. Existing DB data preserved. Cause: {}", ex.getMessage());
        }
    }
}
