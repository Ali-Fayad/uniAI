package com.uniai.catalog.infrastructure.persistence.adapter;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.domain.repository.UniversityCatalogRepository;
import com.uniai.catalog.infrastructure.persistence.repository.UniversityCatalogJpaRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
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
        return canonicalize(jpaRepository.findByNameContainingIgnoreCaseOrAcronymContainingIgnoreCaseOrNameArContainingIgnoreCaseOrderByNameAsc(
                        search,
                        search,
                        search
                ));
    }

    private List<UniversityCatalog> canonicalize(List<UniversityCatalog> rows) {
        Map<String, UniversityCatalog> selected = new LinkedHashMap<>();
        for (UniversityCatalog row : rows) {
            String key = canonicalKey(row);
            UniversityCatalog current = selected.get(key);
            if (current == null || score(row) < score(current)
                    || (score(row) == score(current) && row.getId() != null && current.getId() != null && row.getId() < current.getId())) {
                selected.put(key, row);
            }
        }
        return selected.values().stream()
                .sorted(Comparator.comparing(UniversityCatalog::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(UniversityCatalog::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    private int score(UniversityCatalog row) {
        if (row.getCampusName() == null || row.getCampusName().isBlank()) return 0;
        return row.getCampusType() != null && row.getCampusType().equalsIgnoreCase("main") ? 1 : 2;
    }

    private String canonicalKey(UniversityCatalog row) {
        String acronym = row.getAcronym();
        if (acronym != null && !acronym.isBlank()) return "acronym:" + acronym.trim().toLowerCase(Locale.ROOT);
        return "name:" + (row.getName() == null ? "" : row.getName().trim().toLowerCase(Locale.ROOT));
    }
}
