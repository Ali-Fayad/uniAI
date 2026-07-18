package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.citation.GraduateKnowledgeRetrievalResult;
import com.uniai.chat.application.port.out.GraduateLocationRetrievalPort;
import com.uniai.chat.application.retrieval.GraduateKnowledgeFilters;
import com.uniai.chat.application.retrieval.GraduateKnowledgeOperation;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeResource;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Reads only structured location columns; it never uses the canonical catalog projection. */
@Component
public class SqlGraduateLocationRetrievalAdapter implements GraduateLocationRetrievalPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SqlGraduateLocationRetrievalAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public GraduateKnowledgeRetrievalResult retrieveContext(GraduateKnowledgeQuery query) {
        if (query == null || query.ambiguous()) {
            return result("No matching rows were found for the requested location question.");
        }

        GraduateKnowledgeFilters filters = query.filters();
        boolean campusResource = query.resource() == GraduateKnowledgeResource.CAMPUS;
        boolean universityResource = query.resource() == GraduateKnowledgeResource.UNIVERSITY;
        if (!campusResource && !universityResource) {
            return result("Location data is unavailable for this request.");
        }

        if (query.operation() == GraduateKnowledgeOperation.COMPARE
                && query.followUpContext().comparisonDimension() == com.uniai.chat.application.retrieval.GraduateKnowledgeComparisonDimension.CAMPUS_COUNT) {
            return compareCampusCounts(query, filters);
        }

        String field = campusResource ? "campus_name" : "city";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        String scope = buildScope(filters, parameters);
        long availableRows = countRows(scope + " AND NULLIF(BTRIM(" + field + "), '') IS NOT NULL", parameters);
        if (availableRows == 0) {
            return result("Location data is unavailable in the structured university data.");
        }

        String where = scope + " AND NULLIF(BTRIM(" + field + "), '') IS NOT NULL";
        if (filters.city() != null && !filters.city().isBlank()) {
            parameters.addValue("city", filters.city().trim());
            where += " AND LOWER(BTRIM(city)) = LOWER(BTRIM(:city))";
        }
        String sql = "SELECT id, name, acronym, city, campus_name, campus_type "
                + "FROM university WHERE " + where
                + " ORDER BY LOWER(COALESCE(city, '')), LOWER(name), id";
        List<LocationRow> rows = jdbcTemplate.query(sql, parameters, (rs, rowNum) -> new LocationRow(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("acronym"),
                rs.getString("city"),
                rs.getString("campus_name"),
                rs.getString("campus_type")
        ));

        if (rows.isEmpty()) {
            return result("No matching rows were found in the structured university data.");
        }

        List<LocationRow> selected = universityResource ? canonicalInstitutions(rows) : rows;
        return result(format(query.operation(), campusResource, selected));
    }

    private String buildScope(GraduateKnowledgeFilters filters, MapSqlParameterSource parameters) {
        List<String> predicates = new ArrayList<>();
        List<Long> ids = filters.universities().stream()
                .map(ResolvedUniversity::id)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (!ids.isEmpty()) {
            predicates.add("id IN (:universityIds)");
            parameters.addValue("universityIds", ids);
        }
        if (filters.city() != null && !filters.city().isBlank()) {
            predicates.add("LOWER(BTRIM(city)) = LOWER(BTRIM(:city))");
            parameters.addValue("city", filters.city().trim());
        }
        return predicates.isEmpty() ? "1 = 1" : String.join(" AND ", predicates);
    }

    private GraduateKnowledgeRetrievalResult compareCampusCounts(GraduateKnowledgeQuery query, GraduateKnowledgeFilters filters) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        String scope = buildScope(filters, parameters);
        String sql = "SELECT id, name, acronym, COUNT(*) AS campus_count FROM university WHERE " + scope
                + " AND NULLIF(BTRIM(campus_name), '') IS NOT NULL GROUP BY id, name, acronym ORDER BY campus_count DESC, LOWER(name), id LIMIT "
                + (query.limit() == null ? GraduateKnowledgeQuery.MAX_LIMIT : query.limit());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, parameters);
        if (rows.isEmpty()) return result("No comparable campus data was found.");
        StringBuilder builder = new StringBuilder("Campus comparison:\n");
        int ordinal = 1;
        for (Map<String, Object> row : rows) {
            builder.append(ordinal++).append(". ").append(row.get("name"));
            if (row.get("acronym") != null) builder.append(" (").append(row.get("acronym")).append(')');
            builder.append(" - campuses: ").append(row.get("campus_count")).append('\n');
        }
        return result(builder.toString().trim());
    }

    private long countRows(String where, MapSqlParameterSource parameters) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM university WHERE " + where, parameters, Long.class);
        return count == null ? 0L : count;
    }

    private List<LocationRow> canonicalInstitutions(List<LocationRow> rows) {
        Map<String, LocationRow> selected = new LinkedHashMap<>();
        for (LocationRow row : rows) {
            String key = logicalInstitutionKey(row);
            LocationRow current = selected.get(key);
            if (current == null || compareCanonical(row, current) < 0) {
                selected.put(key, row);
            }
        }
        return selected.values().stream()
                .sorted(Comparator.comparing(LocationRow::name, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(LocationRow::id))
                .toList();
    }

    private int compareCanonical(LocationRow left, LocationRow right) {
        int leftScore = isBlank(left.campusName()) ? 0 : isMainCampus(left) ? 1 : 2;
        int rightScore = isBlank(right.campusName()) ? 0 : isMainCampus(right) ? 1 : 2;
        return Integer.compare(leftScore, rightScore);
    }

    private boolean isMainCampus(LocationRow row) {
        return normalize(row.campusName()).contains("main campus")
                || normalize(row.campusType()).contains("main");
    }

    private String logicalInstitutionKey(LocationRow row) {
        String acronym = normalize(row.acronym());
        return acronym.isBlank() ? "name:" + normalize(row.name()) : "acronym:" + acronym;
    }

    private String format(GraduateKnowledgeOperation operation, boolean campusResource, List<LocationRow> rows) {
        String label = campusResource ? "campus" : "university";
        if (operation == GraduateKnowledgeOperation.COUNT) {
            return "Structured " + label + " count: " + rows.size() + ".";
        }
        if (operation == GraduateKnowledgeOperation.EXISTS) {
            return "A matching structured " + label + " exists in the university data.";
        }
        StringBuilder builder = new StringBuilder("Structured university location results:\n");
        for (LocationRow row : rows) {
            builder.append("- ").append(row.name());
            if (!isBlank(row.acronym())) {
                builder.append(" (").append(row.acronym()).append(')');
            }
            if (campusResource && !isBlank(row.campusName())) {
                builder.append(" - campus: ").append(row.campusName());
            }
            if (!isBlank(row.city())) {
                builder.append(" - city: ").append(row.city());
            }
            if (!isBlank(row.campusType())) {
                builder.append(" - campus type: ").append(row.campusType());
            }
            builder.append('\n');
        }
        return builder.toString().trim();
    }

    private GraduateKnowledgeRetrievalResult result(String context) {
        return new GraduateKnowledgeRetrievalResult(context, List.of());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record LocationRow(Long id, String name, String acronym, String city, String campusName, String campusType) {
    }
}
