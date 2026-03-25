package com.uniai.catalog.application.service;

import com.uniai.catalog.domain.model.PositionCatalog;
import com.uniai.catalog.domain.model.SkillCatalog;
import com.uniai.catalog.infrastructure.persistence.repository.PositionCatalogJpaRepository;
import com.uniai.catalog.infrastructure.persistence.repository.SkillCatalogJpaRepository;
import com.uniai.cvbuilder.infrastructure.client.PositionsApiClient;
import com.uniai.cvbuilder.infrastructure.client.SkillsApiClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogSyncService {

    private static final Logger logger = LogManager.getLogger(CatalogSyncService.class);

    private final SkillsApiClient skillsApiClient;
    private final PositionsApiClient positionsApiClient;
    private final SkillCatalogJpaRepository skillRepository;
    private final PositionCatalogJpaRepository positionRepository;

    @PostConstruct
    public void initialSync() {
        syncExternalCatalogs();
    }

    @Scheduled(fixedDelayString = "${app.sync.external.fixed-delay-ms:86400000}")
    @CacheEvict(value = {"catalog-skills", "catalog-languages"}, allEntries = true)
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
