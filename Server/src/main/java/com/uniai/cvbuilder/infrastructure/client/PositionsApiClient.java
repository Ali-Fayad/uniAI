package com.uniai.cvbuilder.infrastructure.client;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP client for retrieving popular job positions from the GitHub Jobs API.
 */
@Component
@RequiredArgsConstructor
public class PositionsApiClient {

    private static final Logger logger = LogManager.getLogger(PositionsApiClient.class);

    private final RestTemplate restTemplate;

    @Value("${app.external.positions.url:https://jobs.github.com/positions.json}")
    private String positionsApiUrl;

    public List<String> fetchPositions() {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    positionsApiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            List<Map<String, Object>> body = response.getBody();
            if (body == null) {
                return List.of();
            }
            List<String> positions = new ArrayList<>();
            for (Map<String, Object> item : body) {
                Object title = item.get("title");
                if (title != null) {
                    positions.add(title.toString());
                }
            }
            return positions;
        } catch (Exception ex) {
            logger.warn("Failed to fetch positions: {}", ex.getMessage());
        }
        return List.of();
    }
}
