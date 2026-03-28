package com.uniai.cvbuilder.infrastructure.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Client for retrieving job positions from a static local JSON file.
 */
@Component
public class PositionsApiClient {

    private static final Logger logger = LogManager.getLogger(PositionsApiClient.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.static.positions.file:json/positions.json}")
    private String staticPositionsFile;

    public List<String> fetchPositions() {
        try {
            Path filePath = Paths.get(staticPositionsFile);
            if (!filePath.isAbsolute()) {
                filePath = Paths.get(System.getProperty("user.dir"), staticPositionsFile).normalize();
            }

            if (!Files.exists(filePath)) {
                logger.warn("Positions static file not found: {}", filePath);
                return List.of();
            }

            List<PositionItem> items = objectMapper.readValue(
                    filePath.toFile(),
                    new TypeReference<List<PositionItem>>() {}
            );

            if (items == null) {
                logger.warn("Positions static file is empty: {}", filePath);
                return List.of();
            }

            TreeSet<String> uniqueSorted = new TreeSet<>(Comparator.naturalOrder());
            for (PositionItem item : items) {
                if (item == null) {
                    continue;
                }
                String name = item.getName();
                if (name == null) {
                    continue;
                }
                String normalized = name.trim();
                if (!normalized.isEmpty()) {
                    uniqueSorted.add(normalized);
                }
            }

            List<String> positions = new ArrayList<>(uniqueSorted);
            logger.info("Loaded {} unique positions from static file {}", positions.size(), filePath);
            return positions;
        } catch (Exception ex) {
            logger.warn("Failed to load positions from static file {}: {}", staticPositionsFile, ex.getMessage());
        }
        return List.of();
    }

    public static final class PositionItem {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
