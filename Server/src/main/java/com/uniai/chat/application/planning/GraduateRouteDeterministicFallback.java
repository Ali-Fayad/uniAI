package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniai.catalog.domain.model.CampusCatalog;
import com.uniai.catalog.domain.model.UniversityCatalog;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Narrow, deterministic recovery for campus questions when the provider cannot
 * produce a usable route contract. It deliberately does not become a second
 * general-purpose planner.
 */
public final class GraduateRouteDeterministicFallback {
    private final GraduateRoutePlanParser parser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GraduateRouteDeterministicFallback(GraduateRoutePlanParser parser) {
        this.parser = parser;
    }

    public Optional<ValidatedGraduateRoutePlan<?>> plan(String userMessage,
                                                        List<UniversityCatalog> catalogs) {
        if (userMessage == null || userMessage.isBlank()
                || !userMessage.toLowerCase(Locale.ROOT).contains("campus")) {
            return Optional.empty();
        }
        UniversityCatalog university = uniqueUniversityMention(userMessage, catalogs);
        String city = uniqueCityMention(userMessage, catalogs);
        if (university == null || city == null) return Optional.empty();

        ObjectNode root = objectMapper.createObjectNode();
        root.put("route", GraduateAiRoute.LIST_CAMPUSES.name());
        ObjectNode arguments = root.putObject("arguments");
        arguments.put("university", university.getName());
        arguments.put("city", city);
        try {
            return Optional.of(parser.parse(objectMapper.writeValueAsString(root)));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private UniversityCatalog uniqueUniversityMention(String message, List<UniversityCatalog> catalogs) {
        List<UniversityCatalog> matches = catalogs == null ? List.of() : catalogs.stream()
                .filter(university -> contains(message, university.getName())
                        || contains(message, university.getAcronym())
                        || contains(message, university.getNameAr()))
                .toList();
        return matches.size() == 1 ? matches.get(0) : null;
    }

    private String uniqueCityMention(String message, List<UniversityCatalog> catalogs) {
        List<String> cities = catalogs == null ? List.of() : catalogs.stream()
                .flatMap(university -> university.getCampuses() == null
                        ? java.util.stream.Stream.empty()
                        : university.getCampuses().stream())
                .map(CampusCatalog::getCity)
                .filter(city -> city != null && !city.isBlank() && contains(message, city))
                .map(String::trim)
                .distinct()
                .toList();
        return cities.size() == 1 ? cities.get(0) : null;
    }

    private boolean contains(String message, String candidate) {
        return candidate != null && !candidate.isBlank()
                && message.toLowerCase(Locale.ROOT).contains(candidate.toLowerCase(Locale.ROOT));
    }
}
