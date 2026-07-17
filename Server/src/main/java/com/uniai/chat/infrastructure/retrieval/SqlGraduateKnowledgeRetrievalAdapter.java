package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.citation.GraduateCitation;
import com.uniai.chat.application.citation.GraduateKnowledgeRetrievalResult;
import com.uniai.chat.application.port.out.GraduateKnowledgeRetrievalPort;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import com.uniai.chat.infrastructure.metrics.ChatAiMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SqlGraduateKnowledgeRetrievalAdapter implements GraduateKnowledgeRetrievalPort {

    private static final Logger logger = LogManager.getLogger(SqlGraduateKnowledgeRetrievalAdapter.class);
    private static final int MAX_TEXT_LENGTH = 220;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final MeterRegistry meterRegistry;

    public SqlGraduateKnowledgeRetrievalAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, null);
    }

    @Autowired
    public SqlGraduateKnowledgeRetrievalAdapter(NamedParameterJdbcTemplate jdbcTemplate, MeterRegistry meterRegistry) {
        this.jdbcTemplate = jdbcTemplate;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public GraduateKnowledgeRetrievalResult retrieveContext(GraduateKnowledgeQuery query) {
        long startNanos = System.nanoTime();
        GraduateKnowledgeQuery safeQuery = query == null
                ? new GraduateKnowledgeQuery(GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS, List.of(), List.of(), null, false, true)
                : query;
        String retrievalStrategy = safeStrategy(safeQuery);
        String intent = safeIntent(safeQuery);
        ChatAiMetrics.incrementCounter(
                meterRegistry,
                ChatAiMetrics.RETRIEVAL_REQUESTS,
                "AI retrieval requests",
                "retrieval_strategy",
                retrievalStrategy,
                "intent",
                intent
        );

        logger.debug("[RETRIEVAL] Retrieval started intent={} universityCount={} degreeTypeCount={} followUpResolved={} ambiguous={} detailLevel={}",
                safeQuery.intent(),
                safeQuery.resolvedUniversities().size(),
                safeQuery.degreeTypes().size(),
                safeQuery.followUpResolved(),
                safeQuery.ambiguous(),
                safeQuery.detailLevel());

        try {
            if (safeQuery.intent() == GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS
                    || safeQuery.ambiguous()
                    || safeQuery.resolvedUniversities().isEmpty()) {
                String emptyContext = buildEmptyContext(safeQuery, "Unable to determine a specific graduate-information intent.");
                logger.debug("[RETRIEVAL] Retrieval completed strategy=EMPTY contextLength={} durationMs={}",
                        emptyContext.length(),
                        elapsedMillis(startNanos));
                ChatAiMetrics.incrementCounter(
                        meterRegistry,
                        ChatAiMetrics.RETRIEVAL_EMPTY,
                        "Empty retrieval results",
                        "retrieval_strategy",
                        retrievalStrategy,
                        "intent",
                        intent
                );
                recordRetrievalMetrics(startNanos, retrievalStrategy, intent, "empty", 0L, 0L, emptyContext.length());
                return new GraduateKnowledgeRetrievalResult(emptyContext, List.of());
            }

            if (safeQuery.intent() == GraduateKnowledgeIntent.PROGRAM_LOOKUP) {
                List<ProgramRecord> programs = queryPrograms(safeQuery);
                logger.debug("[RETRIEVAL] Program SQL completed rows={} strategy=PROGRAM_LOOKUP", programs.size());
                List<ProgramRecord> rankedPrograms = rankProgramRecords(safeQuery, programs);
                String context = buildProgramContext(safeQuery, rankedPrograms);
                List<GraduateCitation> citations = buildProgramCitations(rankedPrograms);
                logger.debug("[RETRIEVAL] Retrieval completed strategy=PROGRAM_LOOKUP contextLength={} durationMs={}",
                        context.length(),
                        elapsedMillis(startNanos));
                recordRetrievalMetrics(startNanos, retrievalStrategy, intent, "success", programs.size(), citations.size(), context.length());
                return new GraduateKnowledgeRetrievalResult(context, citations);
            }

            if (safeQuery.intent() == GraduateKnowledgeIntent.TUITION_AGGREGATION) {
                List<TuitionAggregationRecord> tuitionAggregations = queryTuitionAggregations(safeQuery);
                logger.debug("[RETRIEVAL] Tuition aggregation SQL completed rows={} strategy=TUITION_AGGREGATION",
                        tuitionAggregations.size());
                List<TuitionAggregationRecord> rankedTuitionAggregations = rankTuitionAggregations(safeQuery, tuitionAggregations);
                String context = buildTuitionContext(safeQuery, rankedTuitionAggregations);
                List<GraduateCitation> citations = buildTuitionCitations(rankedTuitionAggregations);
                logger.debug("[RETRIEVAL] Retrieval completed strategy=TUITION_AGGREGATION contextLength={} durationMs={}",
                        context.length(),
                        elapsedMillis(startNanos));
                recordRetrievalMetrics(startNanos, retrievalStrategy, intent, "success", tuitionAggregations.size(), citations.size(), context.length());
                return new GraduateKnowledgeRetrievalResult(context, citations);
            }

            if (safeQuery.intent() == GraduateKnowledgeIntent.GRADUATE_OVERVIEW) {
                List<ProgramRecord> programs = queryPrograms(safeQuery);
                logger.debug("[RETRIEVAL] Program SQL completed rows={} strategy=GRADUATE_OVERVIEW", programs.size());
                List<ProgramRecord> rankedPrograms = rankProgramRecords(safeQuery, programs);

                List<TuitionAggregationRecord> tuitionAggregations = queryTuitionAggregations(safeQuery);
                logger.debug("[RETRIEVAL] Tuition aggregation SQL completed rows={} strategy=GRADUATE_OVERVIEW",
                        tuitionAggregations.size());
                List<TuitionAggregationRecord> rankedTuitionAggregations = rankTuitionAggregations(safeQuery, tuitionAggregations);

                String context = buildOverviewContext(safeQuery, rankedPrograms, rankedTuitionAggregations);
                List<GraduateCitation> citations = buildOverviewCitations(rankedPrograms, rankedTuitionAggregations);
                logger.debug("[RETRIEVAL] Retrieval completed strategy=GRADUATE_OVERVIEW contextLength={} durationMs={}",
                        context.length(),
                        elapsedMillis(startNanos));
                recordRetrievalMetrics(
                        startNanos,
                        retrievalStrategy,
                        intent,
                        "success",
                        programs.size() + tuitionAggregations.size(),
                        citations.size(),
                        context.length()
                );
                return new GraduateKnowledgeRetrievalResult(context, citations);
            }

            if (safeQuery.intent() == GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP) {
                return retrieveAcademicStructure(safeQuery, startNanos, retrievalStrategy, intent);
            }

            String emptyContext = buildEmptyContext(safeQuery, "Unable to determine a specific graduate-information intent.");
            logger.debug("[RETRIEVAL] Retrieval completed strategy=EMPTY contextLength={} durationMs={}",
                    emptyContext.length(),
                    elapsedMillis(startNanos));
            ChatAiMetrics.incrementCounter(
                    meterRegistry,
                    ChatAiMetrics.RETRIEVAL_EMPTY,
                    "Empty retrieval results",
                    "retrieval_strategy",
                    retrievalStrategy,
                    "intent",
                    intent
            );
            recordRetrievalMetrics(startNanos, retrievalStrategy, intent, "empty", 0L, 0L, emptyContext.length());
            return new GraduateKnowledgeRetrievalResult(emptyContext, List.of());
        } catch (RuntimeException ex) {
            logger.error("[RETRIEVAL] SQL retrieval failed durationMs={} reason={}", elapsedMillis(startNanos), ex.getMessage(), ex);
            recordRetrievalMetrics(startNanos, retrievalStrategy, intent, "failure", 0L, 0L, 0L);
            throw ex;
        }
    }

    private GraduateKnowledgeRetrievalResult retrieveAcademicStructure(
            GraduateKnowledgeQuery query,
            long startNanos,
            String retrievalStrategy,
            String intent
    ) {
        AcademicRows rows = switch (query.resource()) {
            case PROGRAM -> queryAcademicPrograms(query);
            case FACULTY -> queryAcademicFaculties(query);
            case DEPARTMENT -> queryAcademicDepartments(query);
            default -> AcademicRows.empty();
        };

        boolean empty = rows.rows().isEmpty();
        String context = buildAcademicContext(query, rows, empty);
        List<GraduateCitation> citations = buildAcademicCitations(rows);
        recordRetrievalMetrics(startNanos, retrievalStrategy, intent, empty ? "empty" : "success",
                rows.candidateCount(), rows.rows().size(), context.length());
        return new GraduateKnowledgeRetrievalResult(context, citations);
    }

    private AcademicRows queryAcademicPrograms(GraduateKnowledgeQuery query) {
        AcademicParameters parameters = academicParameters(query);
        String operation = query.operation().name();
        String select = operation.equals("COUNT") || operation.equals("EXISTS")
                ? "SELECT COUNT(DISTINCT gp.id) AS result_count "
                : "SELECT DISTINCT u.id AS university_id, gp.id AS program_id, u.name AS university_name, "
                + "u.acronym AS university_acronym, COALESCE(fac.name, '') AS faculty_name, "
                + "COALESCE(dep.name, '') AS department_name, COALESCE(dt.code, '') AS degree_type_code, "
                + "COALESCE(gp.official_degree_name, '') AS program_name, COALESCE(gp.official_program_url, '') AS official_url, "
                + "COALESCE(src.source_urls, '') AS source_urls ";
        String sql = select + """
                FROM graduate_program gp
                JOIN university u ON u.id = gp.university_id
                LEFT JOIN degree_type dt ON dt.id = gp.degree_type_id
                LEFT JOIN university_faculty fac ON fac.id = gp.faculty_id
                LEFT JOIN university_department dep ON dep.id = gp.department_id
                LEFT JOIN LATERAL (
                    SELECT STRING_AGG(DISTINCT s.url, ' | ' ORDER BY s.url) AS source_urls
                    FROM source s
                    WHERE s.id = gp.source_id
                ) src ON TRUE
                WHERE gp.university_id IN (:universityIds)
                AND (:degreeTypesEmpty = TRUE OR dt.code IN (:degreeTypes))
                AND (:facultyName IS NULL OR LOWER(BTRIM(fac.name)) = LOWER(BTRIM(:facultyName)))
                AND (:departmentName IS NULL OR LOWER(BTRIM(dep.name)) = LOWER(BTRIM(:departmentName)))
                AND (:topicRegex IS NULL OR CONCAT_WS(' ', gp.official_degree_name, gp.major, gp.major_category) ~* :topicRegex)
                """;
        if (operation.equals("COUNT") || operation.equals("EXISTS")) {
            Long count = jdbcTemplate.queryForObject(sql, parameters.values(), Long.class);
            return new AcademicRows(List.of(new AcademicRow(null, null, null, null, null, null, null, count == null ? 0 : count, null, null)), count == null ? 0 : count, "PROGRAM");
        }
        List<AcademicRow> rows = jdbcTemplate.query(sql + " ORDER BY u.name ASC, program_name ASC", parameters.values(), this::mapAcademicRow);
        return new AcademicRows(rows, rows.size(), "PROGRAM");
    }

    private AcademicRows queryAcademicFaculties(GraduateKnowledgeQuery query) {
        AcademicParameters parameters = academicParameters(query);
        boolean scalar = query.operation() == com.uniai.chat.application.retrieval.GraduateKnowledgeOperation.COUNT
                || query.operation() == com.uniai.chat.application.retrieval.GraduateKnowledgeOperation.EXISTS;
        String sql = (scalar ? "SELECT COUNT(DISTINCT fac.id) " : "SELECT DISTINCT u.id AS university_id, u.name AS university_name, u.acronym AS university_acronym, fac.id AS academic_id, fac.name AS academic_name, fac.short_name AS short_name, fac.official_url AS official_url ")
                + "FROM university_faculty fac JOIN university u ON u.id = fac.university_id WHERE fac.university_id IN (:universityIds) AND (:facultyName IS NULL OR LOWER(BTRIM(fac.name)) = LOWER(BTRIM(:facultyName)))";
        if (scalar) {
            Long count = jdbcTemplate.queryForObject(sql, parameters.values(), Long.class);
            return new AcademicRows(List.of(new AcademicRow(null, null, null, null, null, null, null, count == null ? 0 : count, null, null)), count == null ? 0 : count, "FACULTY");
        }
        List<AcademicRow> rows = jdbcTemplate.query(sql + " ORDER BY u.name ASC, fac.name ASC", parameters.values(), this::mapFacultyRow);
        return new AcademicRows(rows, rows.size(), "FACULTY");
    }

    private AcademicRows queryAcademicDepartments(GraduateKnowledgeQuery query) {
        AcademicParameters parameters = academicParameters(query);
        boolean scalar = query.operation() == com.uniai.chat.application.retrieval.GraduateKnowledgeOperation.COUNT
                || query.operation() == com.uniai.chat.application.retrieval.GraduateKnowledgeOperation.EXISTS;
        String sql = (scalar ? "SELECT COUNT(DISTINCT dep.id) " : "SELECT DISTINCT u.id AS university_id, u.name AS university_name, u.acronym AS university_acronym, dep.id AS academic_id, dep.name AS academic_name, dep.short_name AS short_name, dep.official_url AS official_url ")
                + "FROM university_department dep JOIN university u ON u.id = dep.university_id LEFT JOIN university_faculty fac ON fac.id = dep.faculty_id WHERE dep.university_id IN (:universityIds) AND (:departmentName IS NULL OR LOWER(BTRIM(dep.name)) = LOWER(BTRIM(:departmentName))) AND (:facultyName IS NULL OR LOWER(BTRIM(fac.name)) = LOWER(BTRIM(:facultyName)))";
        if (scalar) {
            Long count = jdbcTemplate.queryForObject(sql, parameters.values(), Long.class);
            return new AcademicRows(List.of(new AcademicRow(null, null, null, null, null, null, null, count == null ? 0 : count, null, null)), count == null ? 0 : count, "DEPARTMENT");
        }
        List<AcademicRow> rows = jdbcTemplate.query(sql + " ORDER BY u.name ASC, dep.name ASC", parameters.values(), this::mapDepartmentRow);
        return new AcademicRows(rows, rows.size(), "DEPARTMENT");
    }

    private AcademicParameters academicParameters(GraduateKnowledgeQuery query) {
        List<Long> universityIds = universityIds(query.resolvedUniversities());
        MapSqlParameterSource values = new MapSqlParameterSource()
                .addValue("universityIds", universityIds)
                .addValue("degreeTypes", query.degreeTypes().stream().map(value -> value.toUpperCase(Locale.ROOT)).toList())
                .addValue("degreeTypesEmpty", query.degreeTypes().isEmpty())
                .addValue("facultyName", query.filters().facultyName())
                .addValue("departmentName", query.filters().departmentName())
                .addValue("topicRegex", academicTopicRegex(query.topicKeywords()));
        return new AcademicParameters(values);
    }

    private String academicTopicRegex(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return null;
        }
        return keywords.stream()
                .filter(StringUtils::hasText)
                .limit(5)
                .map(value -> "(^|[^[:alnum:]])" + escapeRegex(value.trim()) + "([^[:alnum:]]|$)")
                .collect(Collectors.joining("|"));
    }

    private String escapeRegex(String value) {
        return value.replaceAll("([\\\\.^$|()\\[\\]{}*+?])", "\\\\$1");
    }

    private AcademicRow mapAcademicRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new AcademicRow(
                rs.getObject("university_id", Long.class),
                rs.getObject("program_id", Long.class),
                rs.getString("university_name"),
                rs.getString("university_acronym"),
                rs.getString("faculty_name"),
                rs.getString("department_name"),
                rs.getString("degree_type_code"),
                1L,
                rs.getString("program_name") == null ? rs.getString("academic_name") : rs.getString("program_name"),
                rs.getString("official_url")
        );
    }

    private AcademicRow mapFacultyRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new AcademicRow(rs.getObject("university_id", Long.class), rs.getObject("academic_id", Long.class),
                rs.getString("university_name"), rs.getString("university_acronym"), null, null, null, 1L,
                rs.getString("academic_name"), rs.getString("official_url"));
    }

    private AcademicRow mapDepartmentRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new AcademicRow(rs.getObject("university_id", Long.class), rs.getObject("academic_id", Long.class),
                rs.getString("university_name"), rs.getString("university_acronym"), null, null, null, 1L,
                rs.getString("academic_name"), rs.getString("official_url"));
    }

    private String buildAcademicContext(GraduateKnowledgeQuery query, AcademicRows result, boolean empty) {
        StringBuilder builder = new StringBuilder();
        appendQueryInterpretation(builder, query);
        appendSectionTitle(builder, query.resource().name());
        if (empty) {
            appendBullet(builder, "Result", query.operation() == com.uniai.chat.application.retrieval.GraduateKnowledgeOperation.EXISTS
                    ? "No matching structured academic data found."
                    : "No structured academic data is available for this request.");
            return builder.toString().trim();
        }
        if (query.operation() == com.uniai.chat.application.retrieval.GraduateKnowledgeOperation.COUNT
                || query.operation() == com.uniai.chat.application.retrieval.GraduateKnowledgeOperation.EXISTS) {
            long count = result.rows().get(0).count();
            appendBullet(builder, "Count", String.valueOf(count));
            appendBullet(builder, "Exists", String.valueOf(count > 0));
            return builder.toString().trim();
        }
        int index = 1;
        for (AcademicRow row : result.rows()) {
            builder.append(index).append(".\n");
            appendIndentedBullet(builder, "Name", row.name());
            if (StringUtils.hasText(row.facultyName())) appendIndentedBullet(builder, "Faculty", row.facultyName());
            if (StringUtils.hasText(row.departmentName())) appendIndentedBullet(builder, "Department", row.departmentName());
            if (StringUtils.hasText(row.degreeType())) appendIndentedBullet(builder, "Degree", row.degreeType());
            if (StringUtils.hasText(row.officialUrl())) appendIndentedBullet(builder, "Official source URL", row.officialUrl());
            appendIndentedBullet(builder, "Citation label", "[S" + index++ + "]");
        }
        return builder.toString().trim();
    }

    private List<GraduateCitation> buildAcademicCitations(AcademicRows result) {
        List<GraduateCitation> citations = new ArrayList<>();
        for (int i = 0; i < result.rows().size(); i++) {
            AcademicRow row = result.rows().get(i);
            if (!StringUtils.hasText(row.officialUrl()) || row.universityId() == null) continue;
            citations.add(new GraduateCitation(
                    "academic-" + row.universityId() + "-" + (row.academicId() == null ? i : row.academicId()),
                    "S" + (i + 1), row.name(), row.officialUrl(), result.resourceType(), row.universityId(), row.universityName(), row.programId(), row.name()
            ));
        }
        return List.copyOf(citations);
    }

    private List<ProgramRecord> queryPrograms(GraduateKnowledgeQuery query) {
        List<Long> universityIds = universityIds(query.resolvedUniversities());
        if (universityIds.isEmpty()) {
            logger.warn("[RETRIEVAL] No university IDs resolved for program lookup");
            return List.of();
        }

        boolean details = query.detailLevel() == GraduateProgramDetailLevel.DETAILS;
        String degreeClause = query.degreeTypes().isEmpty() ? "" : "AND dt.code IN (:degreeTypes)\n";
        String sql = details
                ? programDetailsSql(degreeClause + programFilterClause(query))
                : programListSql(degreeClause + programFilterClause(query));

        MapSqlParameterSource params = baseProgramParams(query);
        logger.debug("[RETRIEVAL] Program SQL execution started universityIdCount={} degreeTypeCount={} detailLevel={}",
                universityIds.size(),
                query.degreeTypes().size(),
                query.detailLevel());
        long startNanos = System.nanoTime();
        List<ProgramRecord> programs = jdbcTemplate.query(sql, params, programRowMapper(details));
        logger.debug("[RETRIEVAL] Program SQL execution completed rows={} durationMs={}",
                programs.size(),
                elapsedMillis(startNanos));
        return programs;
    }

    private List<TuitionAggregationRecord> queryTuitionAggregations(GraduateKnowledgeQuery query) {
        List<Long> universityIds = universityIds(query.resolvedUniversities());
        if (universityIds.isEmpty()) {
            logger.warn("[RETRIEVAL] No university IDs resolved for tuition aggregation");
            return List.of();
        }

        String degreeClause = query.degreeTypes().isEmpty() ? "" : "AND dt.code IN (:degreeTypes)\n";
        String sql = """
                SELECT
                    u.id AS university_id,
                    u.name AS university_name,
                    u.acronym AS university_acronym,
                    COALESCE(dt.code, 'Not available in official data') AS degree_type_code,
                    COALESCE(gtr.currency, 'Not available in official data') AS currency,
                    COALESCE(gtr.billing_basis, 'Not available in official data') AS billing_basis,
                    COALESCE(gtr.academic_year, 'Not available in official data') AS academic_year,
                    COUNT(*) AS record_count,
                    COUNT(gtr.amount) AS numeric_tuition_records_used,
                    AVG(gtr.amount) AS average_amount,
                    COALESCE(STRING_AGG(DISTINCT s.url, ' | ' ORDER BY s.url), 'Not available in official data') AS source_urls
                FROM graduate_tuition_rate gtr
                JOIN university u ON u.id = gtr.university_id
                LEFT JOIN graduate_program gp ON gp.id = gtr.program_id
                LEFT JOIN degree_type dt ON dt.id = gp.degree_type_id
                LEFT JOIN source s ON s.id = gtr.source_id
                WHERE gtr.university_id IN (:universityIds)
                %s
                GROUP BY u.id, u.name, u.acronym, dt.code, gtr.currency, gtr.billing_basis, gtr.academic_year
                ORDER BY u.name ASC, dt.code ASC NULLS LAST, gtr.currency ASC, gtr.billing_basis ASC, gtr.academic_year DESC
                """.formatted(degreeClause);

        MapSqlParameterSource params = baseProgramParams(query);
        logger.debug("[RETRIEVAL] Tuition aggregation SQL execution started universityIdCount={} degreeTypeCount={}",
                universityIds.size(),
                query.degreeTypes().size());
        long startNanos = System.nanoTime();
        List<TuitionAggregationRecord> aggregations = jdbcTemplate.query(sql, params, (rs, rowNum) -> new TuitionAggregationRecord(
                rs.getLong("university_id"),
                rs.getString("university_name"),
                rs.getString("university_acronym"),
                rs.getString("degree_type_code"),
                rs.getString("currency"),
                rs.getString("billing_basis"),
                rs.getString("academic_year"),
                rs.getLong("record_count"),
                rs.getLong("numeric_tuition_records_used"),
                rs.getBigDecimal("average_amount"),
                rs.getString("source_urls")
        ));
        logger.debug("[RETRIEVAL] Tuition aggregation SQL execution completed rows={} durationMs={}",
                aggregations.size(),
                elapsedMillis(startNanos));
        return aggregations;
    }

    private MapSqlParameterSource baseProgramParams(GraduateKnowledgeQuery query) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("universityIds", universityIds(query.resolvedUniversities()));
        if (query.degreeTypes() != null && !query.degreeTypes().isEmpty()) {
            params.addValue("degreeTypes", query.degreeTypes().stream()
                    .filter(StringUtils::hasText)
                    .map(value -> value.trim().toUpperCase(Locale.ROOT))
                    .toList());
        }
        if (!query.filters().languages().isEmpty()) {
            params.addValue("languages", query.filters().languages().stream()
                    .filter(StringUtils::hasText)
                    .map(value -> value.trim().toLowerCase(Locale.ROOT))
                    .toList());
        }
        if (!query.filters().admissionRequirementTypes().isEmpty()) {
            params.addValue("admissionRequirementTypes", query.filters().admissionRequirementTypes().stream()
                    .filter(StringUtils::hasText)
                    .map(value -> value.trim().toUpperCase(Locale.ROOT))
                    .toList());
        }
        if (query.filters().programName() != null) {
            params.addValue("programName", query.filters().programName());
        }
        return params;
    }

    private String programFilterClause(GraduateKnowledgeQuery query) {
        StringBuilder clause = new StringBuilder();
        if (!query.filters().languages().isEmpty()) {
            clause.append("AND (LOWER(BTRIM(l.name)) IN (:languages) "
                    + "OR LOWER(BTRIM(l.code)) IN (:languages) "
                    + "OR LOWER(BTRIM(COALESCE(l.native_name, ''))) IN (:languages))\n");
        }
        if (query.filters().programName() != null) {
            clause.append("AND LOWER(BTRIM(gp.official_degree_name)) = LOWER(BTRIM(:programName))\n");
        }
        if (!query.filters().admissionRequirementTypes().isEmpty()) {
            clause.append("AND EXISTS (SELECT 1 FROM graduate_admission_requirement admission_filter "
                    + "WHERE admission_filter.requirement_type IN (:admissionRequirementTypes) "
                    + "AND (admission_filter.program_id = gp.id "
                    + "OR (admission_filter.program_id IS NULL AND admission_filter.scope_level = 'UNIVERSITY' AND admission_filter.university_id = gp.university_id) "
                    + "OR (admission_filter.program_id IS NULL AND admission_filter.scope_level = 'FACULTY' AND admission_filter.faculty_id = gp.faculty_id) "
                    + "OR (admission_filter.program_id IS NULL AND admission_filter.scope_level = 'DEPARTMENT' AND admission_filter.department_id = gp.department_id)))\n");
        }
        return clause.toString();
    }

    private RowMapper<ProgramRecord> programRowMapper(boolean details) {
        return (rs, rowNum) -> new ProgramRecord(
                rs.getLong("university_id"),
                rs.getLong("program_id"),
                rs.getString("university_name"),
                rs.getString("university_acronym"),
                rs.getString("faculty_name"),
                rs.getString("degree_type_code"),
                rs.getString("official_degree_name"),
                details ? rs.getString("language_name") : null,
                details ? rs.getString("credits") : null,
                details ? rs.getString("delivery_mode") : null,
                details ? rs.getString("thesis_or_non_thesis") : null,
                details ? rs.getString("tuition_summary") : null,
                details ? rs.getString("admission_summary") : null,
                rs.getString("official_program_url"),
                rs.getString("source_urls")
        );
    }

    private String programListSql(String degreeClause) {
        return """
                SELECT
                    u.id AS university_id,
                    gp.id AS program_id,
                    u.name AS university_name,
                    u.acronym AS university_acronym,
                    COALESCE(fac.name, 'Not available in official data') AS faculty_name,
                    COALESCE(dt.code, 'Not available in official data') AS degree_type_code,
                    COALESCE(gp.official_degree_name, 'Not available in official data') AS official_degree_name,
                    COALESCE(gp.official_program_url, 'Not available in official data') AS official_program_url,
                    COALESCE(src.source_urls, 'Not available in official data') AS source_urls
                FROM graduate_program gp
                JOIN university u ON u.id = gp.university_id
                LEFT JOIN degree_type dt ON dt.id = gp.degree_type_id
                LEFT JOIN university_faculty fac ON fac.id = gp.faculty_id
                LEFT JOIN language l ON l.id = gp.primary_language_id
                LEFT JOIN LATERAL (
                    SELECT STRING_AGG(DISTINCT source_url, ' | ' ORDER BY source_url) AS source_urls
                    FROM (
                        SELECT s.url AS source_url
                        FROM source s
                        WHERE s.id = gp.source_id
                        UNION
                        SELECT s2.url AS source_url
                        FROM graduate_program_source gps
                        JOIN source s2 ON s2.id = gps.source_id
                        WHERE gps.program_id = gp.id
                    ) source_union
                ) src ON TRUE
                WHERE gp.university_id IN (:universityIds)
                %s
                ORDER BY u.name ASC, dt.code ASC NULLS LAST, gp.official_degree_name ASC
                """.formatted(degreeClause);
    }

    private String programDetailsSql(String degreeClause) {
        return """
                SELECT
                    u.id AS university_id,
                    gp.id AS program_id,
                    u.name AS university_name,
                    u.acronym AS university_acronym,
                    COALESCE(fac.name, 'Not available in official data') AS faculty_name,
                    COALESCE(dt.code, 'Not available in official data') AS degree_type_code,
                    COALESCE(gp.official_degree_name, 'Not available in official data') AS official_degree_name,
                    COALESCE(l.name, 'Not available in official data') AS language_name,
                    COALESCE(gp.credits::text, 'Not available in official data') AS credits,
                    COALESCE(gp.delivery_mode, 'Not available in official data') AS delivery_mode,
                    COALESCE(gp.thesis_or_non_thesis, 'Not available in official data') AS thesis_or_non_thesis,
                    COALESCE(tuition.tuition_summary, 'Not available in official data') AS tuition_summary,
                    COALESCE(CONCAT_WS(' | ', admissions.admission_summary, documents.document_summary), 'Not available in official data') AS admission_summary,
                    COALESCE(gp.official_program_url, 'Not available in official data') AS official_program_url,
                    COALESCE(src.source_urls, 'Not available in official data') AS source_urls
                FROM graduate_program gp
                JOIN university u ON u.id = gp.university_id
                LEFT JOIN degree_type dt ON dt.id = gp.degree_type_id
                LEFT JOIN university_faculty fac ON fac.id = gp.faculty_id
                LEFT JOIN language l ON l.id = gp.primary_language_id
                LEFT JOIN LATERAL (
                    SELECT STRING_AGG(
                        CONCAT_WS(
                            ': ',
                            COALESCE(gtr.academic_year, 'Not available in official data'),
                            COALESCE(gtr.category, 'Not available in official data'),
                            CONCAT(
                                COALESCE(gtr.amount::text, 'Not available in official data'),
                                ' ',
                                COALESCE(gtr.currency, 'Not available in official data'),
                                ' / ',
                                COALESCE(gtr.billing_basis, 'Not available in official data')
                            )
                        ),
                        ' | ' ORDER BY gtr.academic_year DESC NULLS LAST, gtr.id DESC
                    ) AS tuition_summary
                    FROM graduate_tuition_rate gtr
                    WHERE gtr.program_id = gp.id
                ) tuition ON TRUE
                LEFT JOIN LATERAL (
                    SELECT STRING_AGG(
                        CONCAT_WS(
                            ': ',
                            COALESCE(gar.scope_level, 'UNKNOWN'),
                            COALESCE(gar.requirement_type, 'Not available in official data'),
                            CASE WHEN gar.is_required THEN 'REQUIRED' ELSE 'OPTIONAL' END,
                            CASE WHEN gar.threshold_value IS NOT NULL
                                THEN CONCAT(gar.comparison_operator, ' ', gar.threshold_value, ' ', COALESCE(gar.threshold_unit, ''))
                                ELSE NULL END,
                            LEFT(COALESCE(gar.requirement_text, 'Not available in official data'), 220)
                        ),
                        ' | ' ORDER BY gar.id ASC
                    ) AS admission_summary
                    FROM graduate_admission_requirement gar
                    WHERE gar.program_id = gp.id
                       OR (gar.program_id IS NULL AND gar.scope_level = 'UNIVERSITY' AND gar.university_id = gp.university_id)
                       OR (gar.program_id IS NULL AND gar.scope_level = 'FACULTY' AND gar.faculty_id = gp.faculty_id)
                       OR (gar.program_id IS NULL AND gar.scope_level = 'DEPARTMENT' AND gar.department_id = gp.department_id)
                ) admissions ON TRUE
                LEFT JOIN LATERAL (
                    SELECT STRING_AGG(
                        CONCAT_WS(': ', 'DOCUMENT', grd.scope_level,
                            CASE WHEN grd.is_optional THEN 'OPTIONAL' ELSE 'REQUIRED' END,
                            grd.document_name),
                        ' | ' ORDER BY grd.sort_order ASC NULLS LAST, grd.id ASC
                    ) AS document_summary
                    FROM graduate_required_document grd
                    WHERE grd.program_id = gp.id
                       OR (grd.program_id IS NULL AND grd.scope_level = 'UNIVERSITY' AND grd.university_id = gp.university_id)
                       OR (grd.program_id IS NULL AND grd.scope_level = 'FACULTY' AND grd.faculty_id = gp.faculty_id)
                       OR (grd.program_id IS NULL AND grd.scope_level = 'DEPARTMENT' AND grd.department_id = gp.department_id)
                ) documents ON TRUE
                LEFT JOIN LATERAL (
                    SELECT STRING_AGG(DISTINCT source_url, ' | ' ORDER BY source_url) AS source_urls
                    FROM (
                        SELECT s.url AS source_url
                        FROM source s
                        WHERE s.id = gp.source_id
                        UNION
                        SELECT s2.url AS source_url
                        FROM graduate_program_source gps
                        JOIN source s2 ON s2.id = gps.source_id
                        WHERE gps.program_id = gp.id
                        UNION
                        SELECT s3.url AS source_url
                        FROM graduate_admission_requirement gar2
                        JOIN source s3 ON s3.id = gar2.source_id
                        WHERE gar2.program_id = gp.id
                           OR (gar2.program_id IS NULL AND gar2.scope_level = 'UNIVERSITY' AND gar2.university_id = gp.university_id)
                           OR (gar2.program_id IS NULL AND gar2.scope_level = 'FACULTY' AND gar2.faculty_id = gp.faculty_id)
                           OR (gar2.program_id IS NULL AND gar2.scope_level = 'DEPARTMENT' AND gar2.department_id = gp.department_id)
                        UNION
                        SELECT s4.url AS source_url
                        FROM graduate_required_document grd2
                        JOIN source s4 ON s4.id = grd2.source_id
                        WHERE grd2.program_id = gp.id
                           OR (grd2.program_id IS NULL AND grd2.scope_level = 'UNIVERSITY' AND grd2.university_id = gp.university_id)
                           OR (grd2.program_id IS NULL AND grd2.scope_level = 'FACULTY' AND grd2.faculty_id = gp.faculty_id)
                           OR (grd2.program_id IS NULL AND grd2.scope_level = 'DEPARTMENT' AND grd2.department_id = gp.department_id)
                    ) source_union
                ) src ON TRUE
                WHERE gp.university_id IN (:universityIds)
                %s
                ORDER BY u.name ASC, dt.code ASC NULLS LAST, gp.official_degree_name ASC
                """.formatted(degreeClause);
    }

    private List<ProgramRecord> rankProgramRecords(GraduateKnowledgeQuery query, List<ProgramRecord> programs) {
        if (programs == null || programs.size() <= 1) {
            return programs == null ? List.of() : List.copyOf(programs);
        }

        Map<Long, Integer> universityOrder = universityOrder(query.resolvedUniversities());
        List<IndexedProgramRecord> ranked = new ArrayList<>(programs.size());
        for (int index = 0; index < programs.size(); index++) {
            ranked.add(new IndexedProgramRecord(programs.get(index), index));
        }

        Map<Long, List<IndexedProgramRecord>> buckets = bucketProgramsByUniversity(ranked, universityOrder);
        List<ProgramRecord> ordered = new ArrayList<>(programs.size());
        Comparator<IndexedProgramRecord> comparator = Comparator
                .comparingInt((IndexedProgramRecord candidate) -> programScore(query, candidate.record())).reversed()
                .thenComparing(candidate -> normalize(candidate.record().degreeTypeCode()))
                .thenComparing(candidate -> normalize(candidate.record().officialDegreeName()))
                .thenComparingInt(IndexedProgramRecord::originalIndex);

        for (ResolvedUniversity university : query.resolvedUniversities()) {
            List<IndexedProgramRecord> bucket = buckets.get(university.id());
            if (bucket == null || bucket.isEmpty()) {
                continue;
            }
            bucket.sort(comparator);
            ordered.addAll(bucket.stream().map(IndexedProgramRecord::record).toList());
        }

        if (!buckets.isEmpty()) {
            List<IndexedProgramRecord> unbucketed = buckets.get(null);
            if (unbucketed != null && !unbucketed.isEmpty()) {
                unbucketed.sort(comparator);
                ordered.addAll(unbucketed.stream().map(IndexedProgramRecord::record).toList());
            }
        }

        if (ordered.size() != programs.size()) {
            return List.copyOf(programs);
        }

        logger.debug("[RETRIEVAL] Program ranking applied rankingStrategy=DETERMINISTIC_EVIDENCE buckets={} candidates={}",
                query.resolvedUniversities().size(),
                programs.size());
        return List.copyOf(ordered);
    }

    private List<TuitionAggregationRecord> rankTuitionAggregations(GraduateKnowledgeQuery query, List<TuitionAggregationRecord> aggregations) {
        if (aggregations == null || aggregations.size() <= 1) {
            return aggregations == null ? List.of() : List.copyOf(aggregations);
        }

        Map<Long, Integer> universityOrder = universityOrder(query.resolvedUniversities());
        List<IndexedTuitionAggregationRecord> ranked = new ArrayList<>(aggregations.size());
        for (int index = 0; index < aggregations.size(); index++) {
            ranked.add(new IndexedTuitionAggregationRecord(aggregations.get(index), index));
        }

        Map<Long, List<IndexedTuitionAggregationRecord>> buckets = bucketTuitionByUniversity(ranked, universityOrder);
        List<TuitionAggregationRecord> ordered = new ArrayList<>(aggregations.size());
        Comparator<IndexedTuitionAggregationRecord> comparator = Comparator
                .comparingInt((IndexedTuitionAggregationRecord candidate) -> tuitionScore(candidate.record())).reversed()
                .thenComparing(candidate -> normalize(candidate.record().degreeTypeCode()))
                .thenComparing(candidate -> normalize(candidate.record().currency()))
                .thenComparing(candidate -> normalize(candidate.record().billingBasis()))
                .thenComparing(candidate -> normalize(candidate.record().academicYear()))
                .thenComparingInt(IndexedTuitionAggregationRecord::originalIndex);

        for (ResolvedUniversity university : query.resolvedUniversities()) {
            List<IndexedTuitionAggregationRecord> bucket = buckets.get(university.id());
            if (bucket == null || bucket.isEmpty()) {
                continue;
            }
            bucket.sort(comparator);
            ordered.addAll(bucket.stream().map(IndexedTuitionAggregationRecord::record).toList());
        }

        List<IndexedTuitionAggregationRecord> unbucketed = buckets.get(null);
        if (unbucketed != null && !unbucketed.isEmpty()) {
            unbucketed.sort(comparator);
            ordered.addAll(unbucketed.stream().map(IndexedTuitionAggregationRecord::record).toList());
        }

        if (ordered.size() != aggregations.size()) {
            return List.copyOf(aggregations);
        }

        logger.debug("[RETRIEVAL] Tuition ranking applied rankingStrategy=DETERMINISTIC_EVIDENCE buckets={} candidates={}",
                query.resolvedUniversities().size(),
                aggregations.size());
        return List.copyOf(ordered);
    }

    private Map<Long, Integer> universityOrder(List<ResolvedUniversity> universities) {
        Map<Long, Integer> order = new LinkedHashMap<>();
        if (universities == null) {
            return order;
        }
        for (int index = 0; index < universities.size(); index++) {
            ResolvedUniversity university = universities.get(index);
            if (university != null && university.id() != null && !order.containsKey(university.id())) {
                order.put(university.id(), index);
            }
        }
        return order;
    }

    private Map<Long, List<IndexedProgramRecord>> bucketProgramsByUniversity(
            List<IndexedProgramRecord> programs,
            Map<Long, Integer> universityOrder
    ) {
        Map<Long, List<IndexedProgramRecord>> buckets = new LinkedHashMap<>();
        for (IndexedProgramRecord candidate : programs) {
            Long universityId = candidate.record().universityId();
            if (universityId == null || !universityOrder.containsKey(universityId)) {
                buckets.computeIfAbsent(null, ignored -> new ArrayList<>()).add(candidate);
                continue;
            }
            buckets.computeIfAbsent(universityId, ignored -> new ArrayList<>()).add(candidate);
        }
        return buckets;
    }

    private Map<Long, List<IndexedTuitionAggregationRecord>> bucketTuitionByUniversity(
            List<IndexedTuitionAggregationRecord> aggregations,
            Map<Long, Integer> universityOrder
    ) {
        Map<Long, List<IndexedTuitionAggregationRecord>> buckets = new LinkedHashMap<>();
        for (IndexedTuitionAggregationRecord candidate : aggregations) {
            Long universityId = candidate.record().universityId();
            if (universityId == null || !universityOrder.containsKey(universityId)) {
                buckets.computeIfAbsent(null, ignored -> new ArrayList<>()).add(candidate);
                continue;
            }
            buckets.computeIfAbsent(universityId, ignored -> new ArrayList<>()).add(candidate);
        }
        return buckets;
    }

    private int programScore(GraduateKnowledgeQuery query, ProgramRecord record) {
        int score = 0;
        if (query.degreeTypes() != null && !query.degreeTypes().isEmpty()
                && query.degreeTypes().stream().anyMatch(degreeType -> degreeType != null
                && degreeType.trim().equalsIgnoreCase(record.degreeTypeCode()))) {
            score += 6;
        }
        if (StringUtils.hasText(record.officialDegreeName())) {
            score += 4;
        }
        if (StringUtils.hasText(record.facultyName())) {
            score += 2;
        }
        if (StringUtils.hasText(record.officialProgramUrl())) {
            score += 4;
        }
        score += Math.min(sourceUrlCount(record.sourceUrls()), 3) * 2;
        if (query.detailLevel() == GraduateProgramDetailLevel.DETAILS) {
            score += textScore(record.languageName());
            score += textScore(record.credits());
            score += textScore(record.deliveryMode());
            score += textScore(record.thesisOrNonThesis());
            score += textScore(record.tuitionSummary());
            score += textScore(record.admissionSummary());
        }
        return score;
    }

    private int tuitionScore(TuitionAggregationRecord record) {
        int score = 0;
        if (record.averageTuition() != null) {
            score += 6;
        }
        if (record.numericTuitionRecordsUsed() > 0) {
            score += Math.min((int) record.numericTuitionRecordsUsed(), 3) * 2;
        }
        if (record.recordCount() > 0) {
            score += Math.min((int) record.recordCount(), 3);
        }
        score += Math.min(sourceUrlCount(record.sourceUrls()), 3) * 2;
        return score;
    }

    private int sourceUrlCount(String value) {
        if (!StringUtils.hasText(value) || "Not available in official data".equalsIgnoreCase(value.trim())) {
            return 0;
        }
        int count = 0;
        for (String url : value.split("\\s*\\|\\s*")) {
            if (StringUtils.hasText(url)) {
                count++;
            }
        }
        return count;
    }

    private int textScore(String value) {
        return StringUtils.hasText(value) && !"Not available in official data".equalsIgnoreCase(value.trim()) ? 1 : 0;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : "";
    }

    private String buildProgramContext(GraduateKnowledgeQuery query, List<ProgramRecord> programs) {
        if (programs.isEmpty()) {
            return buildEmptyContext(query, "No matching official data found.");
        }

        StringBuilder builder = new StringBuilder();
        appendQueryInterpretation(builder, query);
        appendProgramsSection(builder, programs, query.detailLevel() == GraduateProgramDetailLevel.DETAILS);
        appendSourcesSection(builder, programs);
        appendMissingDataSection(builder, query, programs);
        return builder.toString().trim();
    }

    private String buildTuitionContext(GraduateKnowledgeQuery query, List<TuitionAggregationRecord> aggregations) {
        StringBuilder builder = new StringBuilder();
        appendQueryInterpretation(builder, query);
        appendTuitionSection(builder, aggregations);
        appendTuitionSourcesSection(builder, aggregations);
        appendTuitionMissingDataSection(builder, aggregations, query);
        return builder.toString().trim();
    }

    private String buildOverviewContext(
            GraduateKnowledgeQuery query,
            List<ProgramRecord> programs,
            List<TuitionAggregationRecord> aggregations
    ) {
        StringBuilder builder = new StringBuilder();
        appendQueryInterpretation(builder, query);

        int citationIndex = 1;
        citationIndex = appendProgramsSection(
                builder,
                programs,
                false,
                true,
                citationIndex,
                "No graduate programs are currently available in the official data."
        );
        appendTuitionSection(builder, aggregations, true, citationIndex);
        appendOverviewSourcesSection(builder, programs, aggregations);
        appendOverviewMissingDataSection(builder, query, programs, aggregations);
        return builder.toString().trim();
    }

    private String buildEmptyContext(GraduateKnowledgeQuery query, String note) {
        StringBuilder builder = new StringBuilder();
        appendQueryInterpretation(builder, query);
        appendSectionTitle(builder, "Missing/Unavailable data");
        appendBullet(builder, "Result", note);
        return builder.toString().trim();
    }

    private void appendQueryInterpretation(StringBuilder builder, GraduateKnowledgeQuery query) {
        appendSectionTitle(builder, "Query interpretation");
        appendBullet(builder, "Intent", query.intent().name());
        appendBullet(builder, "Resolved universities", formatResolvedUniversities(query.resolvedUniversities()));
        appendBullet(builder, "Degree types", formatDegreeTypes(query.degreeTypes()));
        appendBullet(builder, "Follow-up resolved", String.valueOf(query.followUpResolved()));
        appendBullet(builder, "Ambiguous", String.valueOf(query.ambiguous()));
        appendBullet(builder, "Detail level", query.detailLevel() == null ? "Not available in official data" : query.detailLevel().name());
    }

    private int appendProgramsSection(StringBuilder builder, List<ProgramRecord> programs, boolean details) {
        return appendProgramsSection(builder, programs, details, false, 1, "No matching official data found.");
    }

    private int appendProgramsSection(
            StringBuilder builder,
            List<ProgramRecord> programs,
            boolean details,
            boolean includeCitationLabels,
            int nextCitationIndex,
            String emptyMessage
    ) {
        appendSectionTitle(builder, "Programs");
        if (programs.isEmpty()) {
            appendBullet(builder, "Result", emptyMessage);
            return nextCitationIndex;
        }

        int index = nextCitationIndex;
        int cursor = 0;
        while (cursor < programs.size()) {
            int groupEnd = cursor + 1;
            while (groupEnd < programs.size()
                    && sameProgramCompressionGroup(programs.get(groupEnd - 1), programs.get(groupEnd), details)) {
                groupEnd++;
            }

            ProgramRecord first = programs.get(cursor);
            appendProgramCompressionHeader(builder, first, details);
            for (int i = cursor; i < groupEnd; i++) {
                ProgramRecord program = programs.get(i);
                if (includeCitationLabels) {
                    appendIndentedBullet(builder, "Citation label", "[S" + index + "]");
                }
                builder.append(index++).append(".\n");
                appendIndentedBullet(builder, "Program name", program.officialDegreeName());
                if (details) {
                    appendIndentedBullet(builder, "Credits", program.credits());
                    appendIndentedBullet(builder, "Tuition summary", program.tuitionSummary());
                    appendIndentedBullet(builder, "Admission summary", program.admissionSummary());
                }
                appendIndentedBullet(builder, "Official source URL(s)", program.sourceUrls());
                appendIndentedBullet(builder, "Official program URL", program.officialProgramUrl());
            }

            cursor = groupEnd;
        }
        return index;
    }

    private void appendSourcesSection(StringBuilder builder, List<ProgramRecord> programs) {
        appendSectionTitle(builder, "Sources");
        Set<String> sources = new LinkedHashSet<>();
        for (ProgramRecord program : programs) {
            addSourceUrls(sources, program.sourceUrls());
            addSourceUrls(sources, program.officialProgramUrl());
        }
        if (sources.isEmpty()) {
            appendBullet(builder, "Result", "Not available in official data");
            return;
        }
        for (String source : sources) {
            appendBullet(builder, "Source URL", source);
        }
    }

    private int appendTuitionSection(StringBuilder builder, List<TuitionAggregationRecord> aggregations) {
        return appendTuitionSection(builder, aggregations, false, 1);
    }

    private int appendTuitionSection(
            StringBuilder builder,
            List<TuitionAggregationRecord> aggregations,
            boolean includeCitationLabels,
            int nextCitationIndex
    ) {
        appendSectionTitle(builder, "Tuition aggregation");
        if (aggregations.isEmpty()) {
            appendBullet(builder, "Result", "Average tuition is not computable from the official stored data.");
            return nextCitationIndex;
        }

        int index = nextCitationIndex;
        int cursor = 0;
        while (cursor < aggregations.size()) {
            int groupEnd = cursor + 1;
            while (groupEnd < aggregations.size()
                    && sameTuitionCompressionGroup(aggregations.get(groupEnd - 1), aggregations.get(groupEnd))) {
                groupEnd++;
            }

            TuitionAggregationRecord first = aggregations.get(cursor);
            appendBullet(builder, "University", formatUniversity(first.universityName(), first.universityAcronym()));
            int degreeCursor = cursor;
            while (degreeCursor < groupEnd) {
                int degreeEnd = degreeCursor + 1;
                while (degreeEnd < groupEnd
                        && sameTuitionDegreeGroup(aggregations.get(degreeEnd - 1), aggregations.get(degreeEnd))) {
                    degreeEnd++;
                }

                TuitionAggregationRecord degreeFirst = aggregations.get(degreeCursor);
                appendIndentedBullet(builder, "Degree type", degreeFirst.degreeTypeCode());
                appendIndentedBullet(builder, "Currency", degreeFirst.currency());
                appendIndentedBullet(builder, "Billing basis", formatBillingBasis(degreeFirst.billingBasis()));
                appendIndentedBullet(builder, "Academic year", formatAcademicYear(degreeFirst.academicYear()));
                for (int i = degreeCursor; i < degreeEnd; i++) {
                    TuitionAggregationRecord aggregation = aggregations.get(i);
                    if (includeCitationLabels) {
                        appendIndentedBullet(builder, "Citation label", "[S" + index + "]");
                    }
                    builder.append(index++).append(".\n");
                    appendIndentedBullet(builder, "Record count", String.valueOf(aggregation.recordCount()));
                    appendIndentedBullet(builder, "Numeric tuition records used", String.valueOf(aggregation.numericTuitionRecordsUsed()));
                    appendIndentedBullet(builder, "Computed average", formatAverage(
                            aggregation.averageTuition(),
                            aggregation.currency(),
                            aggregation.billingBasis(),
                            aggregation.academicYear()
                    ));
                    appendIndentedBullet(builder, "Source URLs", aggregation.sourceUrls());
                }

                degreeCursor = degreeEnd;
            }

            cursor = groupEnd;
        }
        return index;
    }

    private void appendProgramCompressionHeader(StringBuilder builder, ProgramRecord program, boolean details) {
        appendBullet(builder, "University", formatUniversity(program.universityName(), program.universityAcronym()));
        appendIndentedBullet(builder, "Faculty/school", program.facultyName());
        appendIndentedBullet(builder, "Degree type", program.degreeTypeCode());
        if (details) {
            appendIndentedBullet(builder, "Language", program.languageName());
            appendIndentedBullet(builder, "Delivery mode", program.deliveryMode());
            appendIndentedBullet(builder, "Thesis status", program.thesisOrNonThesis());
        }
    }

    private boolean sameProgramCompressionGroup(ProgramRecord previous, ProgramRecord current, boolean details) {
        if (previous == null || current == null) {
            return false;
        }
        return Objects.equals(previous.universityId(), current.universityId())
                && sameText(previous.universityName(), current.universityName())
                && sameText(previous.universityAcronym(), current.universityAcronym())
                && sameText(previous.facultyName(), current.facultyName())
                && sameText(previous.degreeTypeCode(), current.degreeTypeCode())
                && (!details || (
                sameText(previous.languageName(), current.languageName())
                        && sameText(previous.deliveryMode(), current.deliveryMode())
                        && sameText(previous.thesisOrNonThesis(), current.thesisOrNonThesis())
        ));
    }

    private boolean sameTuitionCompressionGroup(TuitionAggregationRecord previous, TuitionAggregationRecord current) {
        if (previous == null || current == null) {
            return false;
        }
        return Objects.equals(previous.universityId(), current.universityId())
                && sameText(previous.universityName(), current.universityName())
                && sameText(previous.universityAcronym(), current.universityAcronym());
    }

    private boolean sameTuitionDegreeGroup(TuitionAggregationRecord previous, TuitionAggregationRecord current) {
        if (previous == null || current == null) {
            return false;
        }
        return sameText(previous.degreeTypeCode(), current.degreeTypeCode())
                && sameText(previous.currency(), current.currency())
                && sameText(previous.billingBasis(), current.billingBasis())
                && sameText(previous.academicYear(), current.academicYear());
    }

    private boolean sameText(String left, String right) {
        return Objects.equals(normalizeText(left), normalizeText(right));
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private void appendTuitionSourcesSection(StringBuilder builder, List<TuitionAggregationRecord> aggregations) {
        appendSectionTitle(builder, "Sources");
        Set<String> sources = new LinkedHashSet<>();
        for (TuitionAggregationRecord aggregation : aggregations) {
            addSourceUrls(sources, aggregation.sourceUrls());
        }
        if (sources.isEmpty()) {
            appendBullet(builder, "Result", "Not available in official data");
            return;
        }
        for (String source : sources) {
            appendBullet(builder, "Source URL", source);
        }
    }

    private void appendOverviewSourcesSection(
            StringBuilder builder,
            List<ProgramRecord> programs,
            List<TuitionAggregationRecord> aggregations
    ) {
        appendSectionTitle(builder, "Sources");
        Set<String> sources = new LinkedHashSet<>();
        for (ProgramRecord program : programs) {
            addSourceUrls(sources, program.sourceUrls());
            addSourceUrls(sources, program.officialProgramUrl());
        }
        for (TuitionAggregationRecord aggregation : aggregations) {
            addSourceUrls(sources, aggregation.sourceUrls());
        }
        if (sources.isEmpty()) {
            appendBullet(builder, "Result", "Not available in official data");
            return;
        }
        for (String source : sources) {
            appendBullet(builder, "Source URL", source);
        }
    }

    private void appendOverviewMissingDataSection(
            StringBuilder builder,
            GraduateKnowledgeQuery query,
            List<ProgramRecord> programs,
            List<TuitionAggregationRecord> aggregations
    ) {
        Set<String> notes = new LinkedHashSet<>();
        if (query.resolvedUniversities().isEmpty()) {
            notes.add("Resolved universities: Not available in official data");
        }
        if (programs.isEmpty()) {
            notes.add("Programs: No graduate programs are currently available in the official data.");
        }
        for (ProgramRecord program : programs) {
            addIfMissing(notes, "Faculty/school", program.facultyName());
            if (query.detailLevel() == GraduateProgramDetailLevel.DETAILS) {
                addIfMissing(notes, "Language", program.languageName());
                addIfMissing(notes, "Credits", program.credits());
                addIfMissing(notes, "Delivery mode", program.deliveryMode());
                addIfMissing(notes, "Thesis status", program.thesisOrNonThesis());
                addIfMissing(notes, "Tuition summary", program.tuitionSummary());
                addIfMissing(notes, "Admission summary", program.admissionSummary());
            }
            addIfMissing(notes, "Official program URL", program.officialProgramUrl());
            addIfMissing(notes, "Official source URL(s)", program.sourceUrls());
        }
        if (aggregations.isEmpty()) {
            notes.add("Tuition aggregation: Average tuition is not computable from the official stored data.");
        }
        for (TuitionAggregationRecord aggregation : aggregations) {
            addIfMissing(notes, "Tuition aggregation source URLs", aggregation.sourceUrls());
            if (aggregation.averageTuition() == null) {
                notes.add("Tuition aggregation: Average tuition is not computable from the official stored data.");
            }
        }
        if (notes.isEmpty()) {
            return;
        }
        appendSectionTitle(builder, "Missing/Unavailable data");
        for (String note : notes) {
            appendBullet(builder, "Result", note);
        }
    }

    private List<GraduateCitation> buildProgramCitations(List<ProgramRecord> programs) {
        if (programs == null || programs.isEmpty()) {
            return List.of();
        }

        List<GraduateCitation> citations = new ArrayList<>(programs.size());
        for (int index = 0; index < programs.size(); index++) {
            ProgramRecord program = programs.get(index);
            if (program == null) {
                continue;
            }
            citations.add(new GraduateCitation(
                    buildProgramCitationId(program, index + 1),
                    "S" + (index + 1),
                    buildProgramCitationTitle(program),
                    buildPrimaryProgramCitationUrl(program),
                    "PROGRAM",
                    program.universityId(),
                    program.universityName(),
                    program.programId(),
                    program.officialDegreeName()
            ));
        }
        return List.copyOf(citations);
    }

    private List<GraduateCitation> buildTuitionCitations(List<TuitionAggregationRecord> aggregations) {
        if (aggregations == null || aggregations.isEmpty()) {
            return List.of();
        }

        List<GraduateCitation> citations = new ArrayList<>(aggregations.size());
        for (int index = 0; index < aggregations.size(); index++) {
            TuitionAggregationRecord aggregation = aggregations.get(index);
            if (aggregation == null) {
                continue;
            }
            citations.add(new GraduateCitation(
                    buildTuitionCitationId(aggregation, index + 1),
                    "S" + (index + 1),
                    buildTuitionCitationTitle(aggregation),
                    firstSourceUrl(aggregation.sourceUrls()),
                    "TUITION",
                    aggregation.universityId(),
                    aggregation.universityName(),
                    null,
                    aggregation.degreeTypeCode()
            ));
        }
        return List.copyOf(citations);
    }

    private List<GraduateCitation> buildOverviewCitations(
            List<ProgramRecord> programs,
            List<TuitionAggregationRecord> aggregations
    ) {
        List<GraduateCitation> merged = new ArrayList<>();
        merged.addAll(buildProgramCitations(programs));
        merged.addAll(buildTuitionCitations(aggregations));
        return renumberCitations(merged);
    }

    private List<GraduateCitation> renumberCitations(List<GraduateCitation> citations) {
        if (citations == null || citations.isEmpty()) {
            return List.of();
        }

        List<GraduateCitation> renumbered = new ArrayList<>(citations.size());
        for (int index = 0; index < citations.size(); index++) {
            GraduateCitation citation = citations.get(index);
            if (citation == null) {
                continue;
            }
            renumbered.add(new GraduateCitation(
                    citation.citationId(),
                    "S" + (index + 1),
                    citation.title(),
                    citation.url(),
                    citation.sourceType(),
                    citation.universityId(),
                    citation.universityName(),
                    citation.programId(),
                    citation.programName()
            ));
        }
        return List.copyOf(renumbered);
    }

    private String buildProgramCitationId(ProgramRecord program, int index) {
        return "program-" + safeId(program.universityId()) + "-" + safeId(program.programId()) + "-" + index;
    }

    private String buildTuitionCitationId(TuitionAggregationRecord aggregation, int index) {
        return "tuition-" + safeId(aggregation.universityId())
                + "-" + safeCitationSegment(aggregation.degreeTypeCode())
                + "-" + safeCitationSegment(aggregation.currency())
                + "-" + safeCitationSegment(aggregation.billingBasis())
                + "-" + safeCitationSegment(aggregation.academicYear())
                + "-" + index;
    }

    private String buildProgramCitationTitle(ProgramRecord program) {
        String university = formatUniversity(program.universityName(), program.universityAcronym());
        if (!StringUtils.hasText(program.officialDegreeName())) {
            return university;
        }
        return university + " " + program.officialDegreeName();
    }

    private String buildTuitionCitationTitle(TuitionAggregationRecord aggregation) {
        String university = formatUniversity(aggregation.universityName(), aggregation.universityAcronym());
        String scope = formatTuitionScope(aggregation.currency(), aggregation.billingBasis(), aggregation.academicYear());
        if (!StringUtils.hasText(aggregation.degreeTypeCode())
                || "Not available in official data".equalsIgnoreCase(aggregation.degreeTypeCode().trim())) {
            return university + " tuition (" + scope + ")";
        }
        return university + " " + aggregation.degreeTypeCode().trim() + " tuition (" + scope + ")";
    }

    private String buildPrimaryProgramCitationUrl(ProgramRecord program) {
        String primaryUrl = firstSourceUrl(program.sourceUrls());
        if (StringUtils.hasText(primaryUrl)) {
            return primaryUrl;
        }
        String officialUrl = program.officialProgramUrl();
        return isAvailableUrl(officialUrl) ? officialUrl.trim() : "";
    }

    private String firstSourceUrl(String value) {
        if (!StringUtils.hasText(value) || "Not available in official data".equalsIgnoreCase(value.trim())) {
            return "";
        }
        for (String url : value.split("\\s*\\|\\s*")) {
            if (StringUtils.hasText(url)) {
                return url.trim();
            }
        }
        return "";
    }

    private boolean isAvailableUrl(String value) {
        return StringUtils.hasText(value) && !"Not available in official data".equalsIgnoreCase(value.trim());
    }

    private String safeId(Long value) {
        return value == null ? "unknown" : String.valueOf(value);
    }

    private void appendMissingDataSection(StringBuilder builder, GraduateKnowledgeQuery query, List<ProgramRecord> programs) {
        Set<String> notes = new LinkedHashSet<>();
        if (query.resolvedUniversities().isEmpty()) {
            notes.add("Resolved universities: Not available in official data");
        }
        if (query.degreeTypes().isEmpty() && query.intent() == GraduateKnowledgeIntent.PROGRAM_LOOKUP) {
            notes.add("Degree types: Not available in official data");
        }
        for (ProgramRecord program : programs) {
            addIfMissing(notes, "Faculty/school", program.facultyName());
            if (query.detailLevel() == GraduateProgramDetailLevel.DETAILS) {
                addIfMissing(notes, "Language", program.languageName());
                addIfMissing(notes, "Credits", program.credits());
                addIfMissing(notes, "Delivery mode", program.deliveryMode());
                addIfMissing(notes, "Thesis status", program.thesisOrNonThesis());
                addIfMissing(notes, "Tuition summary", program.tuitionSummary());
                addIfMissing(notes, "Admission summary", program.admissionSummary());
            }
            addIfMissing(notes, "Official program URL", program.officialProgramUrl());
            addIfMissing(notes, "Official source URL(s)", program.sourceUrls());
        }
        if (notes.isEmpty()) {
            return;
        }
        appendSectionTitle(builder, "Missing/Unavailable data");
        for (String note : notes) {
            appendBullet(builder, "Result", note);
        }
    }

    private void appendTuitionMissingDataSection(StringBuilder builder, List<TuitionAggregationRecord> aggregations, GraduateKnowledgeQuery query) {
        Set<String> notes = new LinkedHashSet<>();
        if (query.resolvedUniversities().isEmpty()) {
            notes.add("Resolved universities: Not available in official data");
        }
        if (query.degreeTypes().isEmpty() && aggregations.isEmpty()) {
            notes.add("Degree types: Not available in official data");
        }
        if (aggregations.isEmpty()) {
            notes.add("Tuition aggregation: Average tuition is not computable from the official stored data.");
        }
        for (TuitionAggregationRecord aggregation : aggregations) {
            addIfMissing(notes, "Tuition aggregation source URLs", aggregation.sourceUrls());
            if (aggregation.averageTuition() == null) {
                notes.add("Tuition aggregation: Average tuition is not computable from the official stored data.");
            }
        }
        if (notes.isEmpty()) {
            return;
        }
        appendSectionTitle(builder, "Missing/Unavailable data");
        for (String note : notes) {
            appendBullet(builder, "Result", note);
        }
    }

    private void addIfMissing(Set<String> notes, String label, String value) {
        if (!StringUtils.hasText(value) || "Not available in official data".equalsIgnoreCase(value.trim())) {
            notes.add(label + ": Not available in official data");
        }
    }

    private void addSourceUrls(Set<String> sources, String value) {
        if (!StringUtils.hasText(value) || "Not available in official data".equalsIgnoreCase(value.trim())) {
            return;
        }
        for (String url : value.split("\\s*\\|\\s*")) {
            if (StringUtils.hasText(url)) {
                sources.add(url.trim());
            }
        }
    }

    private String formatResolvedUniversities(List<ResolvedUniversity> universities) {
        if (universities == null || universities.isEmpty()) {
            return "Not available in official data";
        }
        return universities.stream()
                .map(university -> formatUniversity(university.name(), university.acronym()))
                .collect(Collectors.joining(" | "));
    }

    private String formatDegreeTypes(List<String> degreeTypes) {
        if (degreeTypes == null || degreeTypes.isEmpty()) {
            return "Not available in official data";
        }
        return String.join(" | ", degreeTypes);
    }

    private String formatUniversity(String universityName, String universityAcronym) {
        if (!StringUtils.hasText(universityName)) {
            return "Not available in official data";
        }
        if (!StringUtils.hasText(universityAcronym)) {
            return universityName;
        }
        return universityName + " (" + universityAcronym + ")";
    }

    private String formatAverage(BigDecimal averageTuition, String currency, String billingBasis, String academicYear) {
        if (averageTuition == null) {
            return "Average tuition is not computable from the official stored data.";
        }
        StringBuilder builder = new StringBuilder(averageTuition.setScale(2, RoundingMode.HALF_UP).toPlainString());
        if (StringUtils.hasText(currency) && !"Not available in official data".equalsIgnoreCase(currency.trim())) {
            builder.append(" ").append(currency.trim());
        }
        String billingBasisLabel = formatBillingBasis(billingBasis);
        if (StringUtils.hasText(billingBasisLabel) && !"Not available in official data".equalsIgnoreCase(billingBasisLabel.trim())) {
            builder.append(" ").append(billingBasisLabel);
        }
        String academicYearLabel = formatAcademicYear(academicYear);
        if (StringUtils.hasText(academicYearLabel) && !"Not available in official data".equalsIgnoreCase(academicYearLabel.trim())) {
            builder.append(" | Academic Year ").append(academicYearLabel);
        }
        return builder.toString();
    }

    private String formatBillingBasis(String billingBasis) {
        if (!StringUtils.hasText(billingBasis) || "Not available in official data".equalsIgnoreCase(billingBasis.trim())) {
            return "Not available in official data";
        }
        return billingBasis.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private String formatAcademicYear(String academicYear) {
        if (!StringUtils.hasText(academicYear) || "Not available in official data".equalsIgnoreCase(academicYear.trim())) {
            return "Not available in official data";
        }
        return academicYear.trim();
    }

    private String formatTuitionScope(String currency, String billingBasis, String academicYear) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(currency) && !"Not available in official data".equalsIgnoreCase(currency.trim())) {
            parts.add(currency.trim());
        }
        String billingBasisLabel = formatBillingBasis(billingBasis);
        if (StringUtils.hasText(billingBasisLabel) && !"Not available in official data".equalsIgnoreCase(billingBasisLabel.trim())) {
            parts.add(billingBasisLabel);
        }
        String academicYearLabel = formatAcademicYear(academicYear);
        if (StringUtils.hasText(academicYearLabel) && !"Not available in official data".equalsIgnoreCase(academicYearLabel.trim())) {
            parts.add("Academic Year " + academicYearLabel);
        }
        if (parts.isEmpty()) {
            return "Not available in official data";
        }
        return String.join(", ", parts);
    }

    private String safeCitationSegment(String value) {
        if (!StringUtils.hasText(value)) {
            return "unknown";
        }
        return normalizeText(value).replaceAll("[^A-Z0-9]+", "-").replaceAll("^-+|-+$", "");
    }

    private List<Long> universityIds(List<ResolvedUniversity> universities) {
        if (universities == null || universities.isEmpty()) {
            return List.of();
        }
        return universities.stream()
                .map(ResolvedUniversity::id)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void appendSectionTitle(StringBuilder builder, String title) {
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(title).append(':').append('\n');
    }

    private void appendBullet(StringBuilder builder, String label, String value) {
        builder.append("- ")
                .append(label)
                .append(": ")
                .append(formatValue(value))
                .append('\n');
    }

    private void appendIndentedBullet(StringBuilder builder, String label, String value) {
        builder.append("  - ")
                .append(label)
                .append(": ")
                .append(formatValue(value))
                .append('\n');
    }

    private String formatValue(String value) {
        if (!StringUtils.hasText(value)) {
            return "Not available in official data";
        }
        String trimmed = value.trim();
        return trimmed.length() > MAX_TEXT_LENGTH
                ? trimmed.substring(0, MAX_TEXT_LENGTH - 1) + "…"
                : trimmed;
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private void recordRetrievalMetrics(long startNanos, String retrievalStrategy, String intent, String outcome, long candidates, long selected, long contextSize) {
        ChatAiMetrics.recordTimer(
                meterRegistry,
                ChatAiMetrics.RETRIEVAL_DURATION,
                "Duration of SQL graduate retrieval",
                System.nanoTime() - startNanos,
                "retrieval_strategy",
                retrievalStrategy,
                "intent",
                intent,
                "outcome",
                outcome
        );
        if (candidates > 0) {
            ChatAiMetrics.recordSummary(
                    meterRegistry,
                    ChatAiMetrics.RANKING_CANDIDATES,
                    "Candidate evidence rows before ranking",
                    "items",
                    candidates,
                    "retrieval_strategy",
                    retrievalStrategy,
                    "intent",
                    intent
            );
        }
        if (selected >= 0) {
            ChatAiMetrics.recordSummary(
                    meterRegistry,
                    ChatAiMetrics.RANKING_SELECTED,
                    "Selected evidence rows after ranking",
                    "items",
                    selected,
                    "retrieval_strategy",
                    retrievalStrategy,
                    "intent",
                    intent
            );
        }
        ChatAiMetrics.recordSummary(
                meterRegistry,
                ChatAiMetrics.CONTEXT_SIZE,
                "Final retrieval context size in characters",
                "characters",
                contextSize,
                "retrieval_strategy",
                retrievalStrategy,
                "intent",
                intent
        );
    }

    private String safeStrategy(GraduateKnowledgeQuery query) {
        if (query == null || query.intent() == null) {
            return "unknown";
        }
        return query.intent().name().toLowerCase(Locale.ROOT);
    }

    private String safeIntent(GraduateKnowledgeQuery query) {
        if (query == null || query.intent() == null) {
            return "unknown";
        }
        return query.intent().name().toLowerCase(Locale.ROOT);
    }

    private record ProgramRecord(
            Long universityId,
            Long programId,
            String universityName,
            String universityAcronym,
            String facultyName,
            String degreeTypeCode,
            String officialDegreeName,
            String languageName,
            String credits,
            String deliveryMode,
            String thesisOrNonThesis,
            String tuitionSummary,
            String admissionSummary,
            String officialProgramUrl,
            String sourceUrls
    ) {
    }

    private record TuitionAggregationRecord(
            Long universityId,
            String universityName,
            String universityAcronym,
            String degreeTypeCode,
            String currency,
            String billingBasis,
            String academicYear,
            long recordCount,
            long numericTuitionRecordsUsed,
            BigDecimal averageTuition,
            String sourceUrls
    ) {
    }

    private record IndexedProgramRecord(ProgramRecord record, int originalIndex) {
    }

    private record IndexedTuitionAggregationRecord(TuitionAggregationRecord record, int originalIndex) {
    }

    private record AcademicParameters(MapSqlParameterSource values) {
    }

    private record AcademicRows(List<AcademicRow> rows, long candidateCount, String resourceType) {
        private AcademicRows {
            rows = rows == null ? List.of() : List.copyOf(rows);
        }

        private static AcademicRows empty() {
            return new AcademicRows(List.of(), 0L, "ACADEMIC");
        }
    }

    private record AcademicRow(
            Long universityId,
            Long programId,
            String universityName,
            String universityAcronym,
            String facultyName,
            String departmentName,
            String degreeType,
            long count,
            String name,
            String officialUrl
    ) {
    }
}
