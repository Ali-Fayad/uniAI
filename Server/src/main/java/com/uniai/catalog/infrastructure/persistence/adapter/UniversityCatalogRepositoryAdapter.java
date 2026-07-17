package com.uniai.catalog.infrastructure.persistence.adapter;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.domain.repository.UniversityCatalogRepository;
import com.uniai.catalog.infrastructure.persistence.repository.UniversityCatalogJpaRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UniversityCatalogRepositoryAdapter implements UniversityCatalogRepository {

    private final UniversityCatalogJpaRepository jpaRepository;

    @Override
    public List<UniversityCatalog> findAll() {
        return canonicalize(jpaRepository.findAll());
    }

    @Override
    public List<UniversityCatalog> searchByName(String search) {
        if (search == null || search.isBlank()) {
            return findAll();
        }
        return canonicalize(
                jpaRepository.findByNameContainingIgnoreCaseOrAcronymContainingIgnoreCaseOrNameArContainingIgnoreCaseOrderByNameAsc(
                        search,
                        search,
                        search
                )
        );
    }

    private List<UniversityCatalog> canonicalize(List<UniversityCatalog> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        Map<String, UniversityCatalog> selected = new LinkedHashMap<>();
        for (UniversityCatalog row : rows) {
            if (row == null) {
                continue;
            }
            String key = canonicalKey(row);
            UniversityCatalog current = selected.get(key);
            if (current == null || compareCanonicalRows(row, current) < 0) {
                selected.put(key, row);
            }
        }

        return selected.values().stream()
                .sorted(Comparator
                        .comparing(UniversityCatalog::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(UniversityCatalog::getAcronym, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(UniversityCatalog::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    private int compareCanonicalRows(UniversityCatalog left, UniversityCatalog right) {
        int leftScore = canonicalScore(left);
        int rightScore = canonicalScore(right);
        if (leftScore != rightScore) {
            return Integer.compare(leftScore, rightScore);
        }
        Long leftId = left.getId();
        Long rightId = right.getId();
        if (leftId == null && rightId == null) {
            return 0;
        }
        if (leftId == null) {
            return 1;
        }
        if (rightId == null) {
            return -1;
        }
        return Long.compare(leftId, rightId);
    }

    private int canonicalScore(UniversityCatalog row) {
        if (row == null) {
            return Integer.MAX_VALUE;
        }
        if (isBlank(row.getCampusName())) {
            return 0;
        }
        if (isMainCampus(row)) {
            return 1;
        }
        return 2;
    }

    private boolean isMainCampus(UniversityCatalog row) {
        String campusName = normalize(row.getCampusName());
        String campusType = normalize(row.getCampusType());
        return campusName.contains("main campus")
                || campusType.contains("main");
    }

    private String canonicalKey(UniversityCatalog row) {
        String acronym = normalize(row.getAcronym());
        if (!acronym.isBlank()) {
            return "acronym:" + acronym;
        }
        String name = normalize(row.getName());
        if (!name.isBlank()) {
            return "name:" + name;
        }
        return "id:" + row.getId();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
