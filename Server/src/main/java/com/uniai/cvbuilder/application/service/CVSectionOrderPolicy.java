package com.uniai.cvbuilder.application.service;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Normalizes the user-configurable CV section order.
 */
@Component
public class CVSectionOrderPolicy {

    private static final List<String> DEFAULT_SECTIONS_ORDER = List.of(
        "education",
        "experience",
        "skills",
        "languages",
        "projects",
        "certificates"
    );

    public List<String> normalize(List<String> sectionsOrder) {
        if (sectionsOrder == null || sectionsOrder.isEmpty()) {
            return DEFAULT_SECTIONS_ORDER;
        }
        return sectionsOrder.stream()
            .filter(section -> section != null && !section.isBlank())
            .map(String::trim)
            .distinct()
            .toList();
    }
}
