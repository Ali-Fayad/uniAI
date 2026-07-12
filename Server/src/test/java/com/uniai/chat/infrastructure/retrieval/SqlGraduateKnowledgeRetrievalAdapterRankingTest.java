package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.citation.GraduateKnowledgeRetrievalResult;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlGraduateKnowledgeRetrievalAdapterRankingTest {

    private RankingNamedParameterJdbcTemplate jdbcTemplate;
    private SqlGraduateKnowledgeRetrievalAdapter adapter;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new RankingNamedParameterJdbcTemplate();
        adapter = new SqlGraduateKnowledgeRetrievalAdapter(jdbcTemplate);
    }

    @Test
    void retrieveContextShouldRankProgramEvidenceWithinUniversityBuckets() {
        jdbcTemplate.programRows = List.of(
                programRow(1L, 11L, "American University of Beirut", "AUB", "Faculty of Arts and Sciences", "MASTER",
                        "Master of Arts in Cultural Studies", null, null, null, null, null, null,
                        "https://aub.edu.lb/cultural-studies", "https://aub.edu.lb/cultural-studies"),
                programRow(1L, 12L, "American University of Beirut", "AUB", "Maroun Semaan Faculty of Engineering and Architecture", "MASTER",
                        "Master of Science in Computer Science", "English", "30", "ON_CAMPUS", "THESIS",
                        "120 USD / credit", "General admission requirements",
                        "https://aub.edu.lb/cs", "https://aub.edu.lb/cs | https://aub.edu.lb/tuition"),
                programRow(2L, 21L, "Université Saint-Joseph", "USJ", "Faculty of Science", "MASTER",
                        "Master in Data Science", "French", "36", "ON_CAMPUS", "NON_THESIS",
                        "Not available in official data", "Not available in official data",
                        "https://usj.edu.lb/data-science", "https://usj.edu.lb/data-science")
        );

        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(
                        new ResolvedUniversity(1L, "American University of Beirut", "AUB"),
                        new ResolvedUniversity(2L, "Université Saint-Joseph", "USJ")
                ),
                List.of("MASTER"),
                GraduateProgramDetailLevel.DETAILS,
                false,
                false
        );

        GraduateKnowledgeRetrievalResult result = adapter.retrieveContext(query);
        String context = result.formattedContext();

        assertTrue(context.contains("Programs:"), context);
        assertTrue(context.contains("Master of Science in Computer Science"), context);
        assertTrue(context.contains("Master of Arts in Cultural Studies"), context);
        assertTrue(context.contains("Master in Data Science"), context);
        assertTrue(context.indexOf("Master of Science in Computer Science") < context.indexOf("Master of Arts in Cultural Studies"), context);
        assertTrue(context.indexOf("Master of Arts in Cultural Studies") < context.indexOf("Master in Data Science"), context);
        assertTrue(context.contains("Official program URL: https://aub.edu.lb/cs"), context);
        assertTrue(context.contains("Official source URL(s): https://aub.edu.lb/cs"), context);
        assertEquals(3, result.citations().size());
        assertEquals("S1", result.citations().get(0).label());
        assertEquals("S2", result.citations().get(1).label());
    }

    @Test
    void retrieveContextShouldRankTuitionEvidenceWithinUniversityBuckets() {
        jdbcTemplate.tuitionRows = List.of(
                tuitionRow(1L, "American University of Beirut", "AUB", "MASTER", "USD", 1L, 0L, null, ""),
                tuitionRow(1L, "American University of Beirut", "AUB", "PHD", "USD", 4L, 3L, new BigDecimal("120.00"),
                        "https://aub.edu.lb/tuition | https://aub.edu.lb/registrar"),
                tuitionRow(2L, "Université Saint-Joseph", "USJ", "MASTER", "EUR", 2L, 2L, new BigDecimal("200.00"),
                        "https://usj.edu.lb/tuition")
        );

        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.TUITION_AGGREGATION,
                List.of(
                        new ResolvedUniversity(1L, "American University of Beirut", "AUB"),
                        new ResolvedUniversity(2L, "Université Saint-Joseph", "USJ")
                ),
                List.of("MASTER", "PHD"),
                null,
                false,
                false
        );

        GraduateKnowledgeRetrievalResult result = adapter.retrieveContext(query);
        String context = result.formattedContext();

        assertTrue(context.contains("Tuition aggregation:"), context);
        assertTrue(context.indexOf("Computed average: 120.00") < context.indexOf("Average tuition is not computable from the official stored data."), context);
        assertTrue(context.contains("Source URLs: https://aub.edu.lb/tuition | https://aub.edu.lb/registrar"), context);
        assertEquals(3, result.citations().size());
    }

    @Test
    void retrieveContextShouldReturnStableEmptyContextForAmbiguousQuery() {
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
        assertEquals(0, jdbcTemplate.queryCount);
        assertEquals(0, result.citations().size());
    }

    private Map<String, Object> programRow(Long universityId, Long programId, String universityName, String universityAcronym,
                                           String facultyName, String degreeTypeCode, String officialDegreeName, String languageName,
                                           String credits, String deliveryMode, String thesisOrNonThesis, String tuitionSummary,
                                           String admissionSummary, String officialProgramUrl, String sourceUrls) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("university_id", universityId);
        row.put("program_id", programId);
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

    private Map<String, Object> tuitionRow(Long universityId, String universityName, String universityAcronym,
                                           String degreeTypeCode, String currency, Long recordCount, Long numericTuitionRecordsUsed,
                                           BigDecimal averageAmount, String sourceUrls) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("university_id", universityId);
        row.put("university_name", universityName);
        row.put("university_acronym", universityAcronym);
        row.put("degree_type_code", degreeTypeCode);
        row.put("currency", currency);
        row.put("record_count", recordCount);
        row.put("numeric_tuition_records_used", numericTuitionRecordsUsed);
        row.put("average_amount", averageAmount);
        row.put("source_urls", sourceUrls);
        return row;
    }

    private final class RankingNamedParameterJdbcTemplate extends NamedParameterJdbcTemplate {
        private List<Map<String, Object>> programRows = List.of();
        private List<Map<String, Object>> tuitionRows = List.of();
        private int queryCount;

        private RankingNamedParameterJdbcTemplate() {
            super(new DriverManagerDataSource());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> List<T> query(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper) {
            queryCount++;
            List<Map<String, Object>> rows = sql.contains("AVG(") ? tuitionRows : programRows;
            List<Map<String, Object>> filtered = filterRows(rows, paramSource);
            List<T> mapped = new ArrayList<>();
            for (int i = 0; i < filtered.size(); i++) {
                try {
                    mapped.add(rowMapper.mapRow(mockResultSet(filtered.get(i)), i));
                } catch (SQLException exception) {
                    throw new IllegalStateException(exception);
                }
            }
            return mapped;
        }
    }

    private List<Map<String, Object>> filterRows(List<Map<String, Object>> rows, SqlParameterSource params) {
        Set<Long> universityIds = toLongSet(value(params, "universityIds"));
        Set<String> degreeTypes = toStringSet(value(params, "degreeTypes"));
        return rows.stream()
                .filter(row -> universityIds.isEmpty() || universityIds.contains(toLong(row.get("university_id"))))
                .filter(row -> degreeTypes.isEmpty() || degreeTypes.contains(stringValue(row.get("degree_type_code")).toUpperCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    private Object value(SqlParameterSource params, String name) {
        if (params instanceof MapSqlParameterSource mapSqlParameterSource) {
            return mapSqlParameterSource.getValue(name);
        }
        return null;
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

    private ResultSet mockResultSet(Map<String, Object> row) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                String methodName = method.getName();
                String column = args != null && args.length > 0 && args[0] != null ? String.valueOf(args[0]) : null;
                return switch (methodName) {
                    case "getString" -> stringValue(row.get(column));
                    case "getLong" -> toLong(row.get(column));
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
