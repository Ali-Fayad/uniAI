package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.citation.GraduateKnowledgeRetrievalResult;
import com.uniai.chat.application.port.out.GraduateLocationRetrievalPort;
import com.uniai.chat.application.retrieval.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Structured location retrieval over the normalized university/campus model. */
@Component
public class SqlGraduateLocationRetrievalAdapter implements GraduateLocationRetrievalPort {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SqlGraduateLocationRetrievalAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public GraduateKnowledgeRetrievalResult retrieveContext(GraduateKnowledgeQuery query) {
        if (query == null || query.ambiguous()) return result("No matching rows were found for the requested location question.");
        boolean campusResource = query.resource() == GraduateKnowledgeResource.CAMPUS;
        boolean universityResource = query.resource() == GraduateKnowledgeResource.UNIVERSITY;
        if (!campusResource && !universityResource) return result("Location data is unavailable for this request.");
        GraduateKnowledgeFilters filters = query.filters();
        if (query.operation() == GraduateKnowledgeOperation.COMPARE
                && query.followUpContext() != null
                && query.followUpContext().comparisonDimension() == GraduateKnowledgeComparisonDimension.CAMPUS_COUNT) {
            return compareCampusCounts(query, filters);
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        String scope = buildScope(filters, parameters);
        String cityPredicate = cityPredicate(filters, parameters);
        String where = scope + " AND " + cityPredicate;
        if (query.operation() == GraduateKnowledgeOperation.COUNT) {
            return count(query, campusResource, where, parameters);
        }
        if (query.operation() == GraduateKnowledgeOperation.EXISTS) {
            return exists(query, campusResource, where, parameters);
        }
        String sql = campusResource
                ? "SELECT u.id AS university_id, u.name, u.acronym, c.city, c.name AS campus_name, c.campus_type "
                    + "FROM campus c JOIN university u ON u.id = c.university_id WHERE " + where
                    + " ORDER BY LOWER(c.city), LOWER(u.name), LOWER(c.name), c.id"
                : "SELECT university_id, name, acronym, city, campus_name, campus_type "
                    + "FROM (SELECT DISTINCT u.id AS university_id, u.name, u.acronym, c.city, "
                    + "NULL AS campus_name, NULL AS campus_type "
                    + "FROM campus c JOIN university u ON u.id = c.university_id WHERE " + where + ") location_rows "
                    + "ORDER BY LOWER(BTRIM(city)), LOWER(name), university_id";
        List<LocationRow> rows = jdbcTemplate.query(sql, parameters, (rs, n) -> new LocationRow(
                rs.getLong("university_id"), rs.getString("name"), rs.getString("acronym"),
                rs.getString("city"), rs.getString("campus_name"), rs.getString("campus_type")));
        if (rows.isEmpty()) return result("No matching rows were found in the structured university/campus data.");
        return result(format(query.operation(), campusResource, rows));
    }

    private String buildScope(GraduateKnowledgeFilters filters, MapSqlParameterSource parameters) {
        List<String> predicates = new ArrayList<>();
        List<Long> ids = filters.universities().stream().map(ResolvedUniversity::id).filter(java.util.Objects::nonNull).toList();
        if (!ids.isEmpty()) {
            predicates.add("c.university_id IN (:universityIds)");
            parameters.addValue("universityIds", ids);
        }
        return predicates.isEmpty() ? "1 = 1" : String.join(" AND ", predicates);
    }

    private String cityPredicate(GraduateKnowledgeFilters filters, MapSqlParameterSource parameters) {
        if (filters.city() == null || filters.city().isBlank()) return "1 = 1";
        parameters.addValue("city", filters.city().trim());
        return "LOWER(BTRIM(c.city)) = LOWER(BTRIM(:city))";
    }

    private GraduateKnowledgeRetrievalResult compareCampusCounts(GraduateKnowledgeQuery query, GraduateKnowledgeFilters filters) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        String sql = "SELECT u.name, u.acronym, COUNT(DISTINCT c.id) AS campus_count FROM campus c JOIN university u ON u.id = c.university_id WHERE "
                + buildScope(filters, parameters) + " AND " + cityPredicate(filters, parameters)
                + " GROUP BY u.id, u.name, u.acronym ORDER BY campus_count DESC, LOWER(u.name), u.id LIMIT "
                + (query.limit() == null ? GraduateKnowledgeQuery.MAX_LIMIT : query.limit());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, parameters);
        if (rows.isEmpty()) return result("No comparable campus data was found.");
        StringBuilder out = new StringBuilder("Campus comparison:\n");
        int ordinal = 1;
        for (Map<String, Object> row : rows) {
            out.append(ordinal++).append(". ").append(row.get("name"));
            if (row.get("acronym") != null) out.append(" (").append(row.get("acronym")).append(')');
            out.append(" - campuses: ").append(row.get("campus_count")).append('\n');
        }
        return result(out.toString().trim());
    }

    private GraduateKnowledgeRetrievalResult count(
            GraduateKnowledgeQuery query,
            boolean campusResource,
            String where,
            MapSqlParameterSource parameters
    ) {
        String entity = campusResource ? "campus" : "university";
        String idExpression = campusResource ? "COUNT(DISTINCT c.id)" : "COUNT(DISTINCT u.id)";
        String from = campusResource
                ? "FROM campus c JOIN university u ON u.id = c.university_id"
                : "FROM campus c JOIN university u ON u.id = c.university_id";
        String sql = "SELECT " + idExpression + " AS total " + from + " WHERE " + where;
        Long total = jdbcTemplate.queryForObject(sql, parameters, Long.class);
        long value = total == null ? 0L : total;
        StringBuilder context = new StringBuilder("Aggregate result:\n")
                .append("Resource: ").append(entity.toUpperCase(Locale.ROOT)).append('\n')
                .append("Operation: COUNT\n")
                .append("Scope: ").append(query.scope()).append('\n')
                .append("Total: ").append(value).append('\n')
                // Retain the established human-readable summary for existing callers.
                .append("Structured ").append(entity).append(" count: ").append(value).append('.');
        String filters = filtersText(query.filters());
        if (filters != null) context.append('\n').append(filters);
        return result(context.toString());
    }

    private GraduateKnowledgeRetrievalResult exists(
            GraduateKnowledgeQuery query,
            boolean campusResource,
            String where,
            MapSqlParameterSource parameters
    ) {
        String entity = campusResource ? "campus" : "university";
        String from = "FROM campus c JOIN university u ON u.id = c.university_id";
        String sql = "SELECT EXISTS (SELECT 1 " + from + " WHERE " + where + ")";
        Boolean exists = jdbcTemplate.queryForObject(sql, parameters, Boolean.class);
        boolean value = Boolean.TRUE.equals(exists);
        String evidence = "";
        if (value) {
            String evidenceSql = campusResource
                    ? "SELECT u.name, u.acronym, c.city, c.name AS campus_name, c.campus_type " + from + " WHERE " + where + " ORDER BY LOWER(c.name), c.id LIMIT 1"
                    : "SELECT u.name, u.acronym, c.city " + from + " WHERE " + where + " ORDER BY LOWER(u.name), u.id LIMIT 1";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(evidenceSql, parameters);
            if (!rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                StringBuilder evidenceBuilder = new StringBuilder("\nMatching evidence: University: ")
                        .append(String.valueOf(row.get("name")));
                if (row.get("acronym") != null) evidenceBuilder.append(" (").append(row.get("acronym")).append(')');
                if (campusResource && row.get("campus_name") != null) evidenceBuilder.append(" | Campus: ").append(row.get("campus_name"));
                if (row.get("city") != null) evidenceBuilder.append(" | City: ").append(row.get("city"));
                if (campusResource && row.get("campus_type") != null) evidenceBuilder.append(" | Campus type: ").append(row.get("campus_type"));
                evidence = evidenceBuilder.toString();
            }
        }
        String condition = conditionText(query.filters(), campusResource);
        return result("Existence result:\n"
                + "Resource: " + entity.toUpperCase(Locale.ROOT) + "\n"
                + "Operation: EXISTS\n"
                + "Scope: " + query.scope() + "\n"
                + "Checked entity: " + entity.toUpperCase(Locale.ROOT) + "\n"
                + "Condition: " + condition + "\n"
                + "Exists: " + value + evidence);
    }

    private String conditionText(GraduateKnowledgeFilters filters, boolean campusResource) {
        List<String> conditions = new ArrayList<>();
        if (filters != null && filters.city() != null && !filters.city().isBlank()) {
            conditions.add("city=" + filters.city().trim());
        }
        if (filters != null && filters.universities() != null && !filters.universities().isEmpty()) {
            String universities = filters.universities().stream()
                    .map(university -> !blank(university.name()) ? university.name() : university.acronym())
                    .filter(value -> value != null && !value.isBlank())
                    .distinct()
                    .reduce((left, right) -> left + ", " + right)
                    .orElse(null);
            if (universities != null) conditions.add("university=" + universities);
        }
        return conditions.isEmpty() ? "any matching structured " + (campusResource ? "campus" : "university") : String.join("; ", conditions);
    }

    private String filtersText(GraduateKnowledgeFilters filters) {
        if (filters == null) return null;
        List<String> values = new ArrayList<>();
        if (filters.city() != null && !filters.city().isBlank()) values.add("City: " + filters.city());
        if (filters.universities() != null && !filters.universities().isEmpty()) {
            String universities = filters.universities().stream()
                    .map(university -> !blank(university.name()) ? university.name() : university.acronym())
                    .filter(value -> value != null && !value.isBlank())
                    .distinct()
                    .reduce((left, right) -> left + ", " + right)
                    .orElse(null);
            if (universities != null) values.add("Universities: " + universities);
        }
        if (!values.isEmpty()) return "Filters: " + String.join(", ", values);
        return null;
    }

    private String format(GraduateKnowledgeOperation operation, boolean campusResource, List<LocationRow> rows) {
        String label = campusResource ? "campus" : "university";
        if (operation == GraduateKnowledgeOperation.COUNT) return "Structured " + label + " count: " + rows.size() + ".";
        if (operation == GraduateKnowledgeOperation.EXISTS) return "A matching structured " + label + " exists in the university data.";
        StringBuilder out = new StringBuilder("Structured university location results:\n");
        for (LocationRow row : rows) {
            out.append("- University: ").append(row.name());
            if (!blank(row.acronym())) out.append(" (").append(row.acronym()).append(')');
            if (campusResource && !blank(row.campusName())) out.append(" | Campus: ").append(row.campusName());
            if (!blank(row.city())) out.append(" | City: ").append(row.city());
            if (!blank(row.campusType())) out.append(" | Campus type: ").append(row.campusType());
            out.append('\n');
        }
        return out.toString().trim();
    }

    private GraduateKnowledgeRetrievalResult result(String context) { return new GraduateKnowledgeRetrievalResult(context, List.of()); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private record LocationRow(Long universityId, String name, String acronym, String city, String campusName, String campusType) {}
}
