package com.uniai.cvbuilder.infrastructure.config;

import com.uniai.cvbuilder.domain.model.University;
import com.uniai.cvbuilder.domain.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CVDataLoader implements ApplicationRunner {

    private static final Logger logger = LogManager.getLogger(CVDataLoader.class);
    private static final String DATA_FILE = "UnisCoordinateTable.md";

    private final UniversityRepository universityRepository;
    private final ResourceLoader resourceLoader;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Resource resource = resolveResource();
            if (resource == null || !resource.exists()) {
                logger.info("Universities seed file not found; skipping load");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                boolean headerSkipped = false;
                while ((line = reader.readLine()) != null) {
                    if (!headerSkipped) {
                        if (line.startsWith("| --")) {
                            headerSkipped = true;
                        }
                        continue;
                    }
                    if (!line.startsWith("|")) {
                        continue;
                    }
                    String[] cols = line.split("\\|");
                    if (cols.length < 8) {
                        continue;
                    }

                    String name = cols[2].trim();
                    String acronym = cols[3].trim();
                    String campusName = cols[4].trim();
                    String coordinates = cols[6].trim();
                    String campusType = cols[7].trim();

                    if (name.isEmpty() || campusName.isEmpty()) {
                        continue;
                    }
                    if (universityRepository.existsByNameAndCampusName(name, campusName)) {
                        continue;
                    }

                    BigDecimal lat = null;
                    BigDecimal lng = null;
                    if (!coordinates.isEmpty()) {
                        String firstPair = coordinates.split(";")[0];
                        String[] parts = firstPair.split(",");
                        if (parts.length >= 2) {
                            try {
                                lat = new BigDecimal(parts[0].trim());
                                lng = new BigDecimal(parts[1].trim());
                            } catch (NumberFormatException ignored) {
                                // skip bad coordinates
                            }
                        }
                    }

                    University university = University.builder()
                            .name(name)
                            .acronym(acronym.isEmpty() ? null : acronym)
                            .campusName(campusName)
                            .campusType(campusType.isEmpty() ? null : campusType)
                            .latitude(lat)
                            .longitude(lng)
                            .build();
                    universityRepository.save(university);
                }
            }
        } catch (Exception ex) {
            logger.warn("Failed to seed universities: {}", ex.getMessage(), ex);
        }
    }

    private Resource resolveResource() {
        Resource classpathResource = new ClassPathResource(DATA_FILE);
        if (classpathResource.exists()) {
            return classpathResource;
        }
        Resource fileResource = resourceLoader.getResource("file:./" + DATA_FILE);
        if (fileResource.exists()) {
            return fileResource;
        }
        Resource repoRootResource = resourceLoader.getResource("file:../" + DATA_FILE);
        if (repoRootResource.exists()) {
            return repoRootResource;
        }
        return null;
    }
}
