package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.citation.GraduateKnowledgeRetrievalResult;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlGraduateKnowledgeRetrievalAdapterTest {

    private SqlGraduateKnowledgeRetrievalAdapter adapter;
    private FakeNamedParameterJdbcTemplate jdbcTemplate;
    private SimpleMeterRegistry meterRegistry;
    private List<Map<String, Object>> tuitionRowsOverride = List.of();

    @BeforeEach
    void setUp() {
        jdbcTemplate = new FakeNamedParameterJdbcTemplate();
        meterRegistry = new SimpleMeterRegistry();
        adapter = new SqlGraduateKnowledgeRetrievalAdapter(jdbcTemplate, meterRegistry);
    }

    @Test
    void retrieveContextShouldReturnOnlyProgramsForRequestedUniversityAndDegreeTypes() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                false,
                false
        );

        GraduateKnowledgeRetrievalResult result = adapter.retrieveContext(query);
        String context = result.formattedContext();

        assertTrue(context.contains("Programs:"), context);
        assertTrue(context.contains("Sources:"), context);
        assertTrue(context.contains("American University of Beirut"), context);
        assertTrue(context.contains("MASTER"), context);
        assertFalse(context.contains("Tuition summary:"), context);
        assertFalse(context.contains("Admission summary:"), context);
        assertTrue(jdbcTemplate.lastSql.contains("graduate_program"), jdbcTemplate.lastSql);
        assertFalse(jdbcTemplate.lastSql.contains("AVG("), jdbcTemplate.lastSql);
        assertEquals(List.of(1L), jdbcTemplate.universityIds());
        assertEquals(List.of("MASTER"), jdbcTemplate.degreeTypes());
        assertEquals(2, result.citations().size());
        assertEquals("S1", result.citations().get(0).label());
        assertEquals(1.0, meterRegistry.find("uniai.retrieval.requests")
                .tags("retrieval_strategy", "program_lookup", "intent", "program_lookup")
                .counter()
                .count());
        assertEquals(1.0, meterRegistry.find("uniai.retrieval.ranking.candidates")
                .tags("retrieval_strategy", "program_lookup", "intent", "program_lookup")
                .summary()
                .count());
        assertEquals(1.0, meterRegistry.find("uniai.retrieval.ranking.selected")
                .tags("retrieval_strategy", "program_lookup", "intent", "program_lookup")
                .summary()
                .count());
    }

    @Test
    void retrieveContextShouldUseDetailsProjectionOnlyWhenRequested() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                GraduateProgramDetailLevel.DETAILS,
                false,
                false
        );

        GraduateKnowledgeRetrievalResult result = adapter.retrieveContext(query);
        String context = result.formattedContext();

        assertTrue(context.contains("Tuition summary:"), context);
        assertTrue(context.contains("Admission summary:"), context);
        assertTrue(jdbcTemplate.lastSql.contains("tuition_summary"), jdbcTemplate.lastSql);
        assertTrue(jdbcTemplate.lastSql.contains("admission_summary"), jdbcTemplate.lastSql);
    }

    @Test
    void retrieveContextShouldCalculateTuitionAggregatesInSqlAndSeparateCurrencies() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.TUITION_AGGREGATION,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                null,
                false,
                false
        );

        GraduateKnowledgeRetrievalResult result = adapter.retrieveContext(query);
        String context = result.formattedContext();

        assertTrue(context.contains("Tuition aggregation:"), context);
        assertTrue(context.contains("Currency: USD"), context);
        assertTrue(context.contains("Currency: EUR"), context);
        assertTrue(context.contains("Billing basis: per credit"), context);
        assertTrue(context.contains("Academic year: 2024-2025"), context);
        assertTrue(context.contains("Computed average: 120.00 USD per credit | Academic Year 2024-2025"), context);
        assertTrue(context.contains("Computed average: 200.00 EUR per credit | Academic Year 2024-2025"), context);
        assertFalse(context.contains("Programs:"), context);
        assertTrue(jdbcTemplate.lastSql.contains("AVG("), jdbcTemplate.lastSql);
        assertTrue(jdbcTemplate.lastSql.contains("GROUP BY"), jdbcTemplate.lastSql);
        assertEquals(List.of(1L), jdbcTemplate.universityIds());
        assertEquals(List.of("MASTER"), jdbcTemplate.degreeTypes());
        assertEquals(2, result.citations().size());
        assertEquals("S1", result.citations().get(0).label());
    }

    @Test
    void retrieveContextShouldSeparateTuitionByBillingBasisAndAcademicYear() {
        tuitionRowsOverride = List.of(
                tuitionRow(101L, 1L, "American University of Beirut", "AUB", "MASTER", "Master of Science in Computer Science", new BigDecimal("100"), "USD", "2024-2025", "Tuition", "PER_CREDIT", "Official tuition sheet", "https://www.aub.edu.lb/fine/cs-masters", "https://www.aub.edu.lb/registrar/tuition"),
                tuitionRow(102L, 1L, "American University of Beirut", "AUB", "MASTER", "Master of Science in Computer Science", new BigDecimal("160"), "USD", "2024-2025", "Tuition", "PER_SEMESTER", "Official tuition sheet", "https://www.aub.edu.lb/fine/cs-masters", "https://www.aub.edu.lb/registrar/tuition"),
                tuitionRow(103L, 1L, "American University of Beirut", "AUB", "MASTER", "Master of Science in Computer Science", new BigDecimal("180"), "USD", "2025-2026", "Tuition", "PER_CREDIT", "Official tuition sheet", "https://www.aub.edu.lb/fine/cs-masters", "https://www.aub.edu.lb/registrar/tuition")
        );

        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.TUITION_AGGREGATION,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                null,
                false,
                false
        );

        GraduateKnowledgeRetrievalResult result = adapter.retrieveContext(query);
        String context = result.formattedContext();

        assertTrue(context.contains("Billing basis: per credit"), context);
        assertTrue(context.contains("Billing basis: per semester"), context);
        assertTrue(context.contains("Academic year: 2024-2025"), context);
        assertTrue(context.contains("Academic year: 2025-2026"), context);
        assertTrue(context.contains("Computed average: 100.00 USD per credit | Academic Year 2024-2025"), context);
        assertTrue(context.contains("Computed average: 160.00 USD per semester | Academic Year 2024-2025"), context);
        assertTrue(context.contains("Computed average: 180.00 USD per credit | Academic Year 2025-2026"), context);
        assertEquals(3, result.citations().size());
    }

    @Test
    void retrieveContextShouldCombineProgramsAndTuitionForGraduateOverview() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.GRADUATE_OVERVIEW,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                null,
                false,
                false
        );

        GraduateKnowledgeRetrievalResult result = adapter.retrieveContext(query);
        String context = result.formattedContext();

        assertTrue(context.contains("Programs:"), context);
        assertTrue(context.contains("Tuition aggregation:"), context);
        assertTrue(context.contains("[S1]"), context);
        assertTrue(context.contains("[S2]"), context);
        assertTrue(context.contains("[S3]"), context);
        assertTrue(context.contains("[S4]"), context);
        assertEquals(2, jdbcTemplate.callCount);
        assertEquals(4, result.citations().size());
        assertEquals("S1", result.citations().get(0).label());
        assertEquals("S4", result.citations().get(3).label());
    }

    @Test
    void retrieveContextShouldReturnNotComputableWhenNoTuitionRowsMatch() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.TUITION_AGGREGATION,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("PHD"),
                null,
                false,
                false
        );

        GraduateKnowledgeRetrievalResult result = adapter.retrieveContext(query);
        String context = result.formattedContext();

        assertTrue(context.contains("Tuition aggregation:"), context);
        assertTrue(context.contains("Average tuition is not computable from the official stored data."), context);
        assertEquals(1, result.citations().size());
    }

    @Test
    void retrieveContextShouldKeepComparisonQueriesBoundedToSelectedUniversities() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(
                        new ResolvedUniversity(1L, "American University of Beirut", "AUB"),
                        new ResolvedUniversity(2L, "Université Saint-Joseph", "USJ")
                ),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                true,
                false
        );

        GraduateKnowledgeRetrievalResult result = adapter.retrieveContext(query);
        String context = result.formattedContext();

        assertTrue(context.contains("American University of Beirut"), context);
        assertTrue(context.contains("Université Saint-Joseph"), context);
        assertFalse(context.contains("Lebanese National Conservatory"), context);
        assertEquals(List.of(1L, 2L), jdbcTemplate.universityIds());
        assertEquals(3, result.citations().size());
    }

    @Test
    void retrieveContextShouldReturnMinimalContextForAmbiguousQueryWithoutBroadRetrieval() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS,
                List.of(),
                List.of(),
                null,
                false,
                true
        );

        GraduateKnowledgeRetrievalResult result = adapter.retrieveContext(query);
        String context = result.formattedContext();

        assertTrue(context.contains("Unable to determine a specific graduate-information intent."), context);
        assertEquals(0, jdbcTemplate.callCount);
        assertEquals(0, result.citations().size());
        assertEquals(1.0, meterRegistry.find("uniai.retrieval.empty")
                .tags("retrieval_strategy", "unknown_or_ambiguous", "intent", "unknown_or_ambiguous")
                .counter()
                .count());
    }

    private List<Map<String, Object>> programRows() {
        return List.of(
                programRow(101L, 1L, "American University of Beirut", "AUB", "Maroun Semaan Faculty of Engineering and Architecture", "MASTER", "Master of Science in Computer Science", "English", "30", "ON_CAMPUS", "THESIS", "120 USD / credit | 140 USD / credit", "General admission requirements", "https://www.aub.edu.lb/fine/cs-masters", "https://www.aub.edu.lb/fine/cs-masters | https://www.aub.edu.lb/registrar/tuition"),
                programRow(102L, 1L, "American University of Beirut", "AUB", "Faculty of Arts and Sciences", "MASTER", "Master of Science in Environmental Policy", "English", "36", "ON_CAMPUS", "THESIS", "200 EUR / credit", "Additional documents", "https://www.aub.edu.lb/fas/environment/master", "https://www.aub.edu.lb/fas/environment/master | https://www.aub.edu.lb/registrar/tuition"),
                programRow(201L, 2L, "Université Saint-Joseph", "USJ", "Faculty of Science", "MASTER", "Master in Data Science", "French", "36", "ON_CAMPUS", "NON_THESIS", "Not available in official data", "Not available in official data", "https://www.usj.edu.lb/programs/data-science", "https://www.usj.edu.lb/programs/data-science"),
                programRow(301L, 1L, "American University of Beirut", "AUB", "Faculty of Arts and Sciences", "PHD", "PhD in Biology", "English", "48", "ON_CAMPUS", "THESIS", "Not available in official data", "Not available in official data", "https://www.aub.edu.lb/fas/biology/phd", "https://www.aub.edu.lb/fas/biology/phd")
        );
    }

    private List<Map<String, Object>> tuitionRows() {
        if (!tuitionRowsOverride.isEmpty()) {
            return tuitionRowsOverride;
        }
        return List.of(
                tuitionRow(101L, 1L, "American University of Beirut", "AUB", "MASTER", "Master of Science in Computer Science", new BigDecimal("100"), "USD", "2024-2025", "Tuition", "PER_CREDIT", "Official tuition sheet", "https://www.aub.edu.lb/fine/cs-masters", "https://www.aub.edu.lb/registrar/tuition"),
                tuitionRow(102L, 1L, "American University of Beirut", "AUB", "MASTER", "Master of Science in Computer Science", new BigDecimal("140"), "USD", "2024-2025", "Tuition", "PER_CREDIT", "Official tuition sheet", "https://www.aub.edu.lb/fine/cs-masters", "https://www.aub.edu.lb/registrar/tuition"),
                tuitionRow(103L, 1L, "American University of Beirut", "AUB", "MASTER", "Master of Science in Environmental Policy", new BigDecimal("200"), "EUR", "2024-2025", "Tuition", "PER_CREDIT", "Official tuition sheet", "https://www.aub.edu.lb/fas/environment/master", "https://www.aub.edu.lb/registrar/tuition"),
                tuitionRow(301L, 1L, "American University of Beirut", "AUB", "PHD", "PhD in Biology", null, "USD", "2024-2025", "Tuition", "PER_CREDIT", "Tuition not published", "https://www.aub.edu.lb/fas/biology/phd", "https://www.aub.edu.lb/fas/biology/phd")
        );
    }

    private Map<String, Object> programRow(Long programId, Long universityId, String universityName, String universityAcronym,
                                           String facultyName, String degreeTypeCode, String officialDegreeName, String languageName,
                                           String credits, String deliveryMode, String thesisOrNonThesis, String tuitionSummary,
                                           String admissionSummary, String officialProgramUrl, String sourceUrls) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("program_id", programId);
        row.put("university_id", universityId);
        row.put("university_name", universityName);
        row.put("university_acronym", universityAcronym);
        row.put("faculty_name", facultyName);
        row.put("degree_type_code", degreeTypeCode);
        row.put("official_degree_name", officialDegreeName);
        row.put("language_name", languageName);
        row.put("credits", credits);
        row.put("delivery_mode", deliveryMode);
        row.put("thesis_or_non_thesis", thesisOrNonThesis);
        row.put("tuition_summary", tuitionSummary);
        row.put("admission_summary", admissionSummary);
        row.put("official_program_url", officialProgramUrl);
        row.put("source_urls", sourceUrls);
        return row;
    }

    private Map<String, Object> tuitionRow(Long programId, Long universityId, String universityName, String universityAcronym,
                                           String degreeTypeCode, String officialDegreeName, BigDecimal amount, String currency,
                                           String academicYear, String category, String billingBasis, String notes,
                                           String officialProgramUrl, String sourceUrls) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("program_id", programId);
        row.put("university_id", universityId);
        row.put("university_name", universityName);
        row.put("university_acronym", universityAcronym);
        row.put("degree_type_code", degreeTypeCode);
        row.put("official_degree_name", officialDegreeName);
        row.put("amount", amount);
        row.put("currency", currency);
        row.put("academic_year", academicYear);
        row.put("category", category);
        row.put("billing_basis", billingBasis);
        row.put("notes", notes);
        row.put("official_program_url", officialProgramUrl);
        row.put("source_urls", sourceUrls);
        return row;
    }

    private final class FakeNamedParameterJdbcTemplate extends NamedParameterJdbcTemplate {
        private String lastSql = "";
        private MapSqlParameterSource lastParams = new MapSqlParameterSource();
        private int callCount;

        private FakeNamedParameterJdbcTemplate() {
            super(new DriverManagerDataSource());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> List<T> query(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper) {
            callCount++;
            lastSql = sql;
            lastParams = (MapSqlParameterSource) paramSource;
            List<Map<String, Object>> rows = sql.contains("AVG(")
                    ? aggregateTuitionRows(lastParams)
                    : filterProgramRows(lastParams);
            List<T> mapped = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                try {
                    mapped.add(rowMapper.mapRow(mockResultSet(rows.get(i)), i));
                } catch (SQLException exception) {
                    throw new IllegalStateException(exception);
                }
            }
            return mapped;
        }

        private List<Long> universityIds() {
            return toLongList(lastParams.getValue("universityIds"));
        }

        private List<String> degreeTypes() {
            return toStringList(lastParams.getValue("degreeTypes"));
        }
    }

    private List<Map<String, Object>> aggregateTuitionRows(MapSqlParameterSource params) {
        List<Map<String, Object>> filtered = filterTuitionRows(params);
        Map<String, List<Map<String, Object>>> grouped = filtered.stream()
                .collect(Collectors.groupingBy(
                        row -> stringValue(row.get("university_id")) + "|" +
                                stringValue(row.get("university_name")) + "|" +
                                stringValue(row.get("university_acronym")) + "|" +
                                stringValue(row.get("degree_type_code")) + "|" +
                                stringValue(row.get("currency")) + "|" +
                                stringValue(row.get("billing_basis")) + "|" +
                                stringValue(row.get("academic_year")),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<Map<String, Object>> aggregated = new ArrayList<>();
        for (List<Map<String, Object>> group : grouped.values()) {
            if (group.isEmpty()) {
                continue;
            }
            Map<String, Object> first = group.get(0);
            long numericCount = group.stream().filter(row -> row.get("amount") != null).count();
            BigDecimal sum = group.stream()
                    .map(row -> toBigDecimal(row.get("amount")))
                    .filter(value -> value != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal average = numericCount > 0 ? sum.divide(BigDecimal.valueOf(numericCount), 2, java.math.RoundingMode.HALF_UP) : null;
            Set<String> sources = group.stream()
                    .flatMap(row -> sourceValues(row.get("source_urls")).stream())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Map<String, Object> aggregatedRow = new LinkedHashMap<>();
            aggregatedRow.put("university_id", first.get("university_id"));
            aggregatedRow.put("university_name", first.get("university_name"));
            aggregatedRow.put("university_acronym", first.get("university_acronym"));
            aggregatedRow.put("degree_type_code", first.get("degree_type_code"));
            aggregatedRow.put("currency", first.get("currency"));
            aggregatedRow.put("record_count", group.size());
            aggregatedRow.put("numeric_tuition_records_used", numericCount);
            aggregatedRow.put("average_amount", average);
            aggregatedRow.put("source_urls", String.join(" | ", sources));
            aggregatedRow.put("program_id", first.get("program_id"));
            aggregatedRow.put("official_degree_name", first.get("official_degree_name"));
            aggregatedRow.put("official_program_url", first.get("official_program_url"));
            aggregatedRow.put("academic_year", first.get("academic_year"));
            aggregatedRow.put("category", first.get("category"));
            aggregatedRow.put("billing_basis", first.get("billing_basis"));
            aggregatedRow.put("notes", first.get("notes"));
            aggregated.add(aggregatedRow);
        }
        return aggregated;
    }

    private List<String> sourceValues(Object value) {
        String text = stringValue(value);
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return List.of(text.split("\\s*\\|\\s*"));
    }

    private List<Map<String, Object>> filterProgramRows(MapSqlParameterSource params) {
        Set<Long> universityIds = toLongSet(params.getValue("universityIds"));
        Set<String> degreeTypes = toStringSet(params.getValue("degreeTypes"));
        return programRows().stream()
                .filter(row -> universityIds.isEmpty() || universityIds.contains(toLong(row.get("university_id"))))
                .filter(row -> degreeTypes.isEmpty() || degreeTypes.contains(stringValue(row.get("degree_type_code")).toUpperCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> filterTuitionRows(MapSqlParameterSource params) {
        Set<Long> universityIds = toLongSet(params.getValue("universityIds"));
        Set<String> degreeTypes = toStringSet(params.getValue("degreeTypes"));
        return tuitionRows().stream()
                .filter(row -> universityIds.isEmpty() || universityIds.contains(toLong(row.get("university_id"))))
                .filter(row -> degreeTypes.isEmpty() || degreeTypes.contains(stringValue(row.get("degree_type_code")).toUpperCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    private ResultSet mockResultSet(Map<String, Object> row) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                String methodName = method.getName();
                String column = args != null && args.length > 0 && args[0] != null ? String.valueOf(args[0]) : null;

                return switch (methodName) {
                    case "getString" -> stringValue(row.get(column));
                    case "getLong" -> toLong(row.get(column));
                    case "getInt" -> (int) toLong(row.get(column));
                    case "getBigDecimal" -> toBigDecimal(row.get(column));
                    case "getObject" -> row.get(column);
                    case "wasNull" -> false;
                    case "isWrapperFor" -> false;
                    case "unwrap" -> null;
                    default -> defaultValue(method.getReturnType());
                };
            }
        };

        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                handler
        );
    }

    private Set<Long> toLongSet(Object value) {
        if (!(value instanceof Collection<?> collection)) {
            return Set.of();
        }
        return collection.stream().map(this::toLong).collect(Collectors.toSet());
    }

    private Set<String> toStringSet(Object value) {
        if (!(value instanceof Collection<?> collection)) {
            return Set.of();
        }
        return collection.stream()
                .map(this::stringValue)
                .filter(text -> text != null && !text.isBlank())
                .map(text -> text.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private List<Long> toLongList(Object value) {
        if (!(value instanceof Collection<?> collection)) {
            return List.of();
        }
        return collection.stream().map(this::toLong).toList();
    }

    private List<String> toStringList(Object value) {
        if (!(value instanceof Collection<?> collection)) {
            return List.of();
        }
        return collection.stream().map(this::stringValue).toList();
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return new BigDecimal(value.toString());
    }

    private Object defaultValue(Class<?> returnType) {
        if (returnType == void.class) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == double.class) {
            return 0D;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }
}
