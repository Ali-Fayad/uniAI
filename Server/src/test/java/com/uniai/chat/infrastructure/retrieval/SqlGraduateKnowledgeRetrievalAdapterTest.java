package com.uniai.chat.infrastructure.retrieval;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.domain.repository.UniversityCatalogRepository;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
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
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlGraduateKnowledgeRetrievalAdapterTest {

    private SqlGraduateKnowledgeRetrievalAdapter adapter;

    @BeforeEach
    void setUp() {
        UniversityCatalogRepository repository = new UniversityCatalogRepository() {
            @Override
            public List<UniversityCatalog> findAll() {
                return universities();
            }

            @Override
            public List<UniversityCatalog> searchByName(String search) {
                if (search == null) {
                    return List.of();
                }
                String normalized = search.toLowerCase(Locale.ROOT);
                return universities().stream()
                        .filter(university -> university.getName().toLowerCase(Locale.ROOT).contains(normalized)
                                || (university.getAcronym() != null && university.getAcronym().toLowerCase(Locale.ROOT).contains(normalized)))
                        .collect(Collectors.toList());
            }
        };
        NamedParameterJdbcTemplate jdbcTemplate = new FakeNamedParameterJdbcTemplate();
        adapter = new SqlGraduateKnowledgeRetrievalAdapter(jdbcTemplate, repository);
    }

    @Test
    void retrieveContextShouldReturnStructuredProgramAndSourceSectionsForAubMasters() {
        String context = adapter.retrieveContext("What master's programs does AUB offer?", List.of());

        assertTrue(context.contains("Programs:"), context);
        assertTrue(context.contains("Sources:"), context);
        assertTrue(context.contains("American University of Beirut"), context);
        assertTrue(context.contains("AUB"), context);
        assertTrue(context.contains("MASTER"), context);
    }

    @Test
    void retrieveContextShouldReturnUnavailableMessageWhenNoOfficialDataMatches() {
        String context = adapter.retrieveContext("Does LNC have PhD programs?", List.of());

        assertTrue(context.contains("No matching official data found."), context);
        assertTrue(context.contains("Missing/Unavailable data:"), context);
    }

    @Test
    void retrieveContextShouldUseRecentConversationForFollowUpUniversityAndDegreeInference() {
        List<AiConversationMessage> history = List.of(
                AiConversationMessage.builder()
                        .role("user")
                        .content("What master's programs does AUB offer?")
                        .build(),
                AiConversationMessage.builder()
                        .role("assistant")
                        .content("Here are the master's programs at AUB.")
                        .build()
        );

        String context = adapter.retrieveContext("What about USJ?", history);

        assertTrue(context.contains("Université Saint-Joseph"), context);
        assertTrue(context.contains("USJ"), context);
        assertTrue(context.contains("MASTER"), context);
        assertFalse(context.contains("No matching official data found."), context);
    }

    @Test
    void retrieveContextShouldIncludeTuitionAggregationWithComputedAverages() {
        String context = adapter.retrieveContext("Give me the average tuition at AUB.", List.of());

        assertTrue(context.contains("Tuition aggregation:"), context);
        assertTrue(context.contains("Computed average: 120.00"), context);
        assertTrue(context.contains("Computed average: 200.00"), context);
    }

    @Test
    void retrieveContextShouldKeepDifferentCurrenciesSeparatedWhenAggregatingTuition() {
        String context = adapter.retrieveContext("Compare tuition at AUB.", List.of());

        assertTrue(context.contains("Currency: USD"), context);
        assertTrue(context.contains("Currency: EUR"), context);
        assertTrue(context.contains("Computed average: 120.00"), context);
        assertTrue(context.contains("Computed average: 200.00"), context);
    }

    @Test
    void retrieveContextShouldReportWhenAverageTuitionCannotBeComputed() {
        String context = adapter.retrieveContext("What is the average tuition for PhD programs?", List.of());

        assertTrue(context.contains("Tuition aggregation:"), context);
        assertTrue(context.contains("Average tuition is not computable from the official stored data."), context);
    }

    @Test
    void retrieveContextShouldNotCapProgramsAtTenForAubTuitionAggregation() {
        String context = adapter.retrieveContext("Give me the average tuition at AUB.", List.of());

        assertTrue(context.contains("Programs considered: 12"), context);
        assertTrue(countOccurrences(context, "  - Program name:") > 10, context);
        assertTrue(context.contains("Computed average: 120.00"), context);
    }

    private List<Map<String, Object>> programRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        IntStream.rangeClosed(1, 12).forEach(index -> {
            long programId = 100L + index;
            BigDecimal amount = index % 2 == 0 ? new BigDecimal("140") : new BigDecimal("100");
            rows.add(programRow(
                    programId,
                    1L,
                    "American University of Beirut",
                    "AUB",
                    "Maroun Semaan Faculty of Engineering and Architecture",
                    "English",
                    "MASTER",
                    "Master of Science in Computer Science " + index,
                    "30",
                    "On campus",
                    "Thesis",
                    amount.stripTrailingZeros().toPlainString() + " USD / credit",
                    "Applicants must hold a relevant bachelor's degree.",
                    "https://www.aub.edu.lb/fine/Pages/cs-masters-" + index + ".aspx",
                    "https://www.aub.edu.lb/fine/Pages/cs-masters-" + index + ".aspx | https://www.aub.edu.lb/registrar/tuition"
            ));
        });

        rows.add(programRow(
                200L,
                1L,
                "American University of Beirut",
                "AUB",
                "Faculty of Arts and Sciences",
                "English",
                "MASTER",
                "Master of Science in Environmental Policy",
                "36",
                "On campus",
                "Thesis",
                "200 EUR / credit",
                "Applicants must hold a relevant bachelor's degree.",
                "https://www.aub.edu.lb/fas/environment/Pages/master.aspx",
                "https://www.aub.edu.lb/fas/environment/Pages/master.aspx | https://www.aub.edu.lb/registrar/tuition"
        ));

        rows.add(programRow(
                300L,
                2L,
                "Université Saint-Joseph",
                "USJ",
                "Faculty of Science",
                "French",
                "MASTER",
                "Master in Data Science",
                "36",
                "On campus",
                "Non-thesis",
                "Not available in official data",
                "Not available in official data",
                "https://www.usj.edu.lb/programs/data-science",
                "https://www.usj.edu.lb/programs/data-science"
        ));

        rows.add(programRow(
                301L,
                1L,
                "American University of Beirut",
                "AUB",
                "Faculty of Arts and Sciences",
                "English",
                "PHD",
                "PhD in Biology",
                "48",
                "On campus",
                "Thesis",
                "Not available in official data",
                "Not available in official data",
                "https://www.aub.edu.lb/fas/biology/Pages/phd.aspx",
                "https://www.aub.edu.lb/fas/biology/Pages/phd.aspx"
        ));

        return rows;
    }

    private List<Map<String, Object>> tuitionRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        IntStream.rangeClosed(1, 12).forEach(index -> {
            BigDecimal amount = index % 2 == 0 ? new BigDecimal("140") : new BigDecimal("100");
            rows.add(tuitionRow(
                    100L + index,
                    1L,
                    "American University of Beirut",
                    "AUB",
                    "MASTER",
                    "Master of Science in Computer Science " + index,
                    amount,
                    "USD",
                    "2024-2025",
                    "Tuition",
                    "Credit",
                    "Official tuition sheet",
                    "https://www.aub.edu.lb/fine/Pages/cs-masters-" + index + ".aspx",
                    "https://www.aub.edu.lb/fine/Pages/cs-masters-" + index + ".aspx | https://www.aub.edu.lb/registrar/tuition"
            ));
        });

        rows.add(tuitionRow(
                200L,
                1L,
                "American University of Beirut",
                "AUB",
                "MASTER",
                "Master of Science in Environmental Policy",
                new BigDecimal("200"),
                "EUR",
                "2024-2025",
                "Tuition",
                "Credit",
                "Official tuition sheet",
                "https://www.aub.edu.lb/fas/environment/Pages/master.aspx",
                "https://www.aub.edu.lb/fas/environment/Pages/master.aspx | https://www.aub.edu.lb/registrar/tuition"
        ));

        rows.add(tuitionRow(
                301L,
                1L,
                "American University of Beirut",
                "AUB",
                "PHD",
                "PhD in Biology",
                null,
                "USD",
                "2024-2025",
                "Tuition",
                "Credit",
                "Tuition not published",
                "https://www.aub.edu.lb/fas/biology/Pages/phd.aspx",
                "https://www.aub.edu.lb/fas/biology/Pages/phd.aspx"
        ));

        return rows;
    }

    private Map<String, Object> programRow(Long programId, Long universityId, String universityName, String universityAcronym,
                                           String facultyName, String languageName, String degreeTypeCode,
                                           String officialDegreeName, String credits, String deliveryMode,
                                           String thesisOrNonThesis, String tuitionSummary, String admissionSummary,
                                           String officialProgramUrl, String sourceUrls) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("program_id", programId);
        row.put("university_id", universityId);
        row.put("university_name", universityName);
        row.put("university_acronym", universityAcronym);
        row.put("faculty_name", facultyName);
        row.put("language_name", languageName);
        row.put("degree_type_code", degreeTypeCode);
        row.put("official_degree_name", officialDegreeName);
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
                                           String degreeTypeCode, String officialDegreeName, BigDecimal amount,
                                           String currency, String academicYear, String category, String billingBasis,
                                           String notes, String officialProgramUrl, String sourceUrls) {
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
        private FakeNamedParameterJdbcTemplate() {
            super(new DriverManagerDataSource());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> List<T> query(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper) {
            MapSqlParameterSource params = (MapSqlParameterSource) paramSource;
            boolean tuitionQuery = sql.contains("gtr.amount AS amount");
            List<Map<String, Object>> rows = tuitionQuery
                    ? filterTuitionRows(params)
                    : filterProgramRows(params);

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
    }

    private List<Map<String, Object>> filterProgramRows(MapSqlParameterSource params) {
        Set<Long> universityIds = toLongSet(params.getValue("universityIds"));
        Set<String> degreeTypes = toStringSet(params.hasValue("degreeTypes") ? params.getValue("degreeTypes") : null);

        return programRows().stream()
                .filter(row -> universityIds.isEmpty() || universityIds.contains(toLong(row.get("university_id"))))
                .filter(row -> degreeTypes.isEmpty() || degreeTypes.contains(stringValue(row.get("degree_type_code")).toUpperCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> filterTuitionRows(MapSqlParameterSource params) {
        Set<Long> universityIds = toLongSet(params.getValue("universityIds"));
        Set<String> degreeTypes = toStringSet(params.hasValue("degreeTypes") ? params.getValue("degreeTypes") : null);

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

    private List<UniversityCatalog> universities() {
        return List.of(
                UniversityCatalog.builder()
                        .id(1L)
                        .name("American University of Beirut")
                        .acronym("AUB")
                        .build(),
                UniversityCatalog.builder()
                        .id(2L)
                        .name("Université Saint-Joseph")
                        .acronym("USJ")
                        .build(),
                UniversityCatalog.builder()
                        .id(3L)
                        .name("Lebanese National Conservatory")
                        .acronym("LNC")
                        .build()
        );
    }

    private Set<Long> toLongSet(Object value) {
        if (!(value instanceof Collection<?> collection)) {
            return Set.of();
        }
        return collection.stream()
                .map(this::toLong)
                .collect(Collectors.toSet());
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

    private long countOccurrences(String text, String token) {
        if (text == null || token == null || token.isEmpty()) {
            return 0L;
        }

        long count = 0L;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }
}
