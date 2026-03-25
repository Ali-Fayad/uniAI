package com.uniai.cvbuilder.infrastructure.client;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP client for retrieving popular skill tags from StackOverflow's public API.
 */
@Component
@RequiredArgsConstructor
public class SkillsApiClient {

    private static final Logger logger = LogManager.getLogger(SkillsApiClient.class);
    private static final String SKILLS_URL = "https://api.stackexchange.com/2.3/tags?site=stackoverflow&pagesize=50&order=desc&sort=popular";

    private final RestTemplate restTemplate;

    public record SkillItem(String name, String category) {}

    public List<String> fetchSkills() {
        List<SkillItem> items = fetchSkillItems();
        List<String> names = new ArrayList<>(items.size());
        for (SkillItem item : items) {
            names.add(item.name());
        }
        return names;
    }

    public List<SkillItem> fetchSkillItems() {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    SKILLS_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("items")) {
                return List.of();
            }
            Object items = body.get("items");
            if (items instanceof List<?> itemList) {
                List<SkillItem> result = new ArrayList<>();
                for (Object item : itemList) {
                    if (item instanceof Map<?, ?> map && map.containsKey("name")) {
                        Object name = map.get("name");
                        if (name != null) {
                            Object category = map.get("category");
                            String categoryValue = category == null ? "General" : category.toString();
                            result.add(new SkillItem(name.toString(), categoryValue));
                        }
                    }
                }
                return result;
            }
        } catch (Exception ex) {
            logger.warn("Failed to fetch skills: {}", ex.getMessage());
        }
        return List.of();
    }
}
