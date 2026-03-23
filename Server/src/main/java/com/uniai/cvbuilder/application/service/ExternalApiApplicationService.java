package com.uniai.cvbuilder.application.service;

import com.uniai.cvbuilder.infrastructure.client.PositionsApiClient;
import com.uniai.cvbuilder.infrastructure.client.SkillsApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service that delegates to external API clients to fetch supplemental data such as
 * popular skills and job positions.
 */
@Service
@RequiredArgsConstructor
public class ExternalApiApplicationService {

    private final SkillsApiClient skillsApiClient;
    private final PositionsApiClient positionsApiClient;

    @Cacheable("cv-skills")
    public List<String> getSkills() {
        return skillsApiClient.fetchSkills();
    }

    @Cacheable("cv-positions")
    public List<String> getPositions() {
        return positionsApiClient.fetchPositions();
    }
}
