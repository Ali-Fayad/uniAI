package com.uniai.cvbuilder.infrastructure.client;

import com.uniai.catalog.application.service.CatalogSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Triggers catalog synchronization from external sources at startup and on a schedule.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.sync.external.catalog.enabled", havingValue = "true", matchIfMissing = true)
public class ExternalCatalogSyncService {

    private final CatalogSyncService catalogSyncService;

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        log.info("Starting initial catalog sync...");
        catalogSyncService.syncExternalCatalogs();
    }

    @Scheduled(cron = "${app.sync.external.catalog.cron:0 0 2 * * *}")
    public void syncDaily() {
        log.info("Starting scheduled catalog sync...");
        catalogSyncService.syncExternalCatalogs();
    }
}
