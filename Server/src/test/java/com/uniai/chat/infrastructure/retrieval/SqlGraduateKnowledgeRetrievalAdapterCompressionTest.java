package com.uniai.chat.infrastructure.retrieval;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlGraduateKnowledgeRetrievalAdapterCompressionTest {

    private CompressionNamedParameterJdbcTemplate jdbcTemplate;
    private SqlGraduateKnowledgeRetrievalAdapter adapter;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new CompressionNamedParameterJdbcTemplate();
        adapter = new SqlGraduateKnowledgeRetrievalAdapter(jdbcTemplate);
    }

    @Test
    void retrieveContextShouldHoistRepeatedProgramMetadataWithoutLosingPrograms() {
        jdbcTemplate.programRows = List.of(
                programRow(1L, 11L, "American University of Beirut", "AUB",
                        "Maroun Semaan Faculty of Engineering and Architecture", "MASTER",
                        "Master of Science in Computer Science", "English", "30", "ON_CAMPUS", "THESIS",
                        "120 USD / credit", "General admission requirements",
                        "https://aub.edu.lb/cs", "https://aub.edu.lb/cs | https://aub.edu.lb/tuition"),
                programRow(1L, 12L, "American University of Beirut", "AUB",
                        "Maroun Semaan Faculty of Engineering and Architecture", "MASTER",
                        "Master of Science in Artificial Intelligence", "English", "30", "ON_CAMPUS", "THESIS",
                        "120 USD / credit", "General admission requirements",
                        "https://aub.edu.lb/ai", "https://aub.edu.lb/ai | https://aub.edu.lb/tuition")
        );

        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                GraduateProgramDetailLevel.DETAILS,
                false,
                false
        );

        String context = adapter.retrieveContext(query);

        assertEquals(1, countOccurrences(context, "Faculty/school: Maroun Semaan Faculty of Engineering and Architecture"), context);
        assertEquals(1, countOccurrences(context, "Language: English"), context);
        assertTrue(context.contains("Official program URL: https://aub.edu.lb/cs"), context);
        assertTrue(context.contains("Official program URL: https://aub.edu.lb/ai"), context);
        assertTrue(context.contains("Official source URL(s): https://aub.edu.lb/cs | https://aub.edu.lb/tuition"), context);
        assertTrue(context.contains("Official source URL(s): https://aub.edu.lb/ai | https://aub.edu.lb/tuition"), context);
        assertEquals(1, countOccurrences(context, "University: American University of Beirut (AUB)"), context);
        assertEquals(1, countOccurrences(context, "Master of Science in Computer Science"), context);
        assertEquals(1, countOccurrences(context, "Master of Science in Artificial Intelligence"), context);
        assertFalse(context.contains("Tuition summary: Not available in official data"), context);
    }

    @Test
    void retrieveContextShouldKeepDifferentProgramFacultiesSeparate() {
        jdbcTemplate.programRows = List.of(
                programRow(1L, 11L, "American University of Beirut", "AUB",
                        "Maroun Semaan Faculty of Engineering and Architecture", "MASTER",
                        "Master of Science in Computer Science", "English", "30", "ON_CAMPUS", "THESIS",
                        "120 USD / credit", "General admission requirements",
                        "https://aub.edu.lb/cs", "https://aub.edu.lb/cs"),
                programRow(1L, 13L, "American University of Beirut", "AUB",
                        "Faculty of Arts and Sciences", "MASTER",
                        "Master of Arts in Media Studies", "English", "36", "ON_CAMPUS", "THESIS",
                        "140 USD / credit", "Portfolio required",
                        "https://aub.edu.lb/media", "https://aub.edu.lb/media")
        );

        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                GraduateProgramDetailLevel.DETAILS,
                false,
                false
        );

        String context = adapter.retrieveContext(query);

        assertEquals(1, countOccurrences(context, "Faculty/school: Maroun Semaan Faculty of Engineering and Architecture"), context);
        assertEquals(1, countOccurrences(context, "Faculty/school: Faculty of Arts and Sciences"), context);
        assertTrue(context.contains("Official program URL: https://aub.edu.lb/cs"), context);
        assertTrue(context.contains("Official program URL: https://aub.edu.lb/media"), context);
        assertEquals(1, countOccurrences(context, "Master of Science in Computer Science"), context);
        assertEquals(1, countOccurrences(context, "Master of Arts in Media Studies"), context);
        assertEquals(2, countOccurrences(context, "University: American University of Beirut (AUB)"), context);
    }

    @Test
    void retrieveContextShouldKeepDifferentProgramDegreesSeparate() {
        jdbcTemplate.programRows = List.of(
                programRow(1L, 11L, "American University of Beirut", "AUB",
                        "Faculty of Arts and Sciences", "MASTER",
                        "Master of Science in Biology", "English", "36", "ON_CAMPUS", "THESIS",
                        "130 USD / credit", "Interview required",
                        "https://aub.edu.lb/biology-master", "https://aub.edu.lb/biology-master"),
                programRow(1L, 12L, "American University of Beirut", "AUB",
                        "Faculty of Arts and Sciences", "PHD",
                        "PhD in Biology", "English", "48", "ON_CAMPUS", "THESIS",
                        "Not available in official data", "Not available in official data",
                        "https://aub.edu.lb/biology-phd", "https://aub.edu.lb/biology-phd")
        );

        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER", "PHD"),
                GraduateProgramDetailLevel.DETAILS,
                false,
                false
        );

        String context = adapter.retrieveContext(query);

        assertEquals(1, countOccurrences(context, "Degree type: MASTER"), context);
        assertEquals(1, countOccurrences(context, "Degree type: PHD"), context);
        assertTrue(context.contains("Official program URL: https://aub.edu.lb/biology-master"), context);
        assertTrue(context.contains("Official program URL: https://aub.edu.lb/biology-phd"), context);
        assertEquals(1, countOccurrences(context, "Master of Science in Biology"), context);
        assertEquals(1, countOccurrences(context, "PhD in Biology"), context);
    }

    @Test
    void retrieveContextShouldCompressTuitionEvidenceByUniversityWithoutDroppingAggregates() {
        jdbcTemplate.tuitionRows = List.of(
                tuitionRow(1L, "American University of Beirut", "AUB", "MASTER", "USD", 10L, 8L, new BigDecimal("24000.00"),
                        "https://aub.edu.lb/tuition | https://aub.edu.lb/registrar"),
                tuitionRow(1L, "American University of Beirut", "AUB", "PHD", "USD", 4L, 3L, new BigDecimal("28000.00"),
                        "https://aub.edu.lb/phd-tuition"),
                tuitionRow(2L, "Université Saint-Joseph", "USJ", "MASTER", "EUR", 7L, 7L, new BigDecimal("12000.00"),
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

        String context = adapter.retrieveContext(query);

        assertEquals(1, countOccurrences(context, "University: American University of Beirut (AUB)"), context);
        assertEquals(1, countOccurrences(context, "University: Université Saint-Joseph (USJ)"), context);
        assertTrue(context.indexOf("University: American University of Beirut (AUB)") < context.indexOf("University: Université Saint-Joseph (USJ)"), context);
        assertTrue(context.contains("Computed average: 24000.00"), context);
        assertTrue(context.contains("Computed average: 28000.00"), context);
        assertTrue(context.contains("Computed average: 12000.00"), context);
        assertTrue(context.contains("Record count: 10"), context);
        assertTrue(context.contains("Numeric tuition records used: 8"), context);
        assertTrue(context.contains("Degree type: MASTER"), context);
        assertTrue(context.contains("Degree type: PHD"), context);
        assertTrue(context.contains("Source URLs: https://aub.edu.lb/tuition | https://aub.edu.lb/registrar"), context);
        assertTrue(context.contains("Source URLs: https://aub.edu.lb/phd-tuition"), context);
        assertTrue(context.contains("Source URLs: https://usj.edu.lb/tuition"), context);
        assertFalse(context.contains("Average tuition is not computable from the official stored data."), context);
    }

    @Test
    void retrieveContextShouldKeepSingleProgramCompleteAndStable() {
        jdbcTemplate.programRows = List.of(
                programRow(1L, 21L, "American University of Beirut", "AUB", "Faculty of Arts and Sciences", "MASTER",
                        "Master of Science in Biology", "English", "36", "ON_CAMPUS", "THESIS",
                        "130 USD / credit", "Interview required",
                        "https://aub.edu.lb/biology", "https://aub.edu.lb/biology")
        );

        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                GraduateProgramDetailLevel.DETAILS,
                false,
                false
        );

        String context = adapter.retrieveContext(query);

        assertTrue(context.contains("Master of Science in Biology"), context);
        assertTrue(context.contains("Credits: 36"), context);
        assertTrue(context.contains("Tuition summary: 130 USD / credit"), context);
        assertTrue(context.contains("Admission summary: Interview required"), context);
        assertTrue(context.contains("Official program URL: https://aub.edu.lb/biology"), context);
        assertTrue(context.contains("Official source URL(s): https://aub.edu.lb/biology"), context);
        assertEquals(1, countOccurrences(context, "Master of Science in Biology"), context);
        assertEquals(1, countOccurrences(context, "University: American University of Beirut (AUB)"), context);
    }

    @Test
    void retrieveContextShouldReturnEmptyContextWhenNoProgramRowsMatch() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(999L, "Missing University", "MU")),
                List.of("MASTER"),
                GraduateProgramDetailLevel.LIST,
                false,
                false
        );

        String context = adapter.retrieveContext(query);

        assertTrue(context.contains("Missing/Unavailable data:"), context);
        assertTrue(context.contains("No matching official data found."), context);
        assertEquals(1, jdbcTemplate.queryCount);
    }

    private int countOccurrences(String text, String needle) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
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

    private final class CompressionNamedParameterJdbcTemplate extends NamedParameterJdbcTemplate {
        private List<Map<String, Object>> programRows = List.of();
        private List<Map<String, Object>> tuitionRows = List.of();
        private int queryCount;

        private CompressionNamedParameterJdbcTemplate() {
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
                .toList();
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
        return collection.stream().map(this::toLong).collect(java.util.stream.Collectors.toSet());
    }

    private Set<String> toStringSet(Object value) {
        if (!(value instanceof Collection<?> collection)) {
            return Set.of();
        }
        return collection.stream()
                .map(this::stringValue)
                .filter(text -> text != null && !text.isBlank())
                .map(text -> text.toUpperCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
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
        return Long.parseLong(String.valueOf(value));
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
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
            return 0f;
        }
        if (returnType == double.class) {
            return 0d;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }
}
