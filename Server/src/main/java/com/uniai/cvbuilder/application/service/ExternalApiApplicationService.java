package com.uniai.cvbuilder.application.service;

import com.uniai.cvbuilder.infrastructure.client.PositionsApiClient;
import com.uniai.cvbuilder.infrastructure.client.SkillsApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalApiApplicationService {

    private final SkillsApiClient skillsApiClient;
    private final PositionsApiClient positionsApiClient;

    public List<String> getSkills() {
        return skillsApiClient.fetchSkills();
    }

    public List<String> getPositions() {
        return positionsApiClient.fetchPositions();
    }
}
