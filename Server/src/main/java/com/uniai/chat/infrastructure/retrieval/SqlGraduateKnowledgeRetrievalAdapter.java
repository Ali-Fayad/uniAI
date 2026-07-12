package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.citation.GraduateCitation;
import com.uniai.chat.application.citation.GraduateKnowledgeRetrievalResult;
import com.uniai.chat.application.port.out.GraduateKnowledgeRetrievalPort;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    public SqlGraduateKnowledgeRetrievalAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public GraduateKnowledgeRetrievalResult retrieveContext(GraduateKnowledgeQuery query) {
        long startNanos = System.nanoTime();
        GraduateKnowledgeQuery safeQuery = query == null
                ? new GraduateKnowledgeQuery(GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS, List.of(), List.of(), null, false, true)
                : query;

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
                return new GraduateKnowledgeRetrievalResult(context, citations);
            }

            String emptyContext = buildEmptyContext(safeQuery, "Unable to determine a specific graduate-information intent.");
            logger.debug("[RETRIEVAL] Retrieval completed strategy=EMPTY contextLength={} durationMs={}",
                    emptyContext.length(),
                    elapsedMillis(startNanos));
            return new GraduateKnowledgeRetrievalResult(emptyContext, List.of());
        } catch (RuntimeException ex) {
            logger.error("[RETRIEVAL] SQL retrieval failed durationMs={} reason={}", elapsedMillis(startNanos), ex.getMessage(), ex);
            throw ex;
        }
    }

    private List<ProgramRecord> queryPrograms(GraduateKnowledgeQuery query) {
        List<Long> universityIds = universityIds(query.resolvedUniversities());
        if (universityIds.isEmpty()) {
            logger.warn("[RETRIEVAL] No university IDs resolved for program lookup");
            return List.of();
        }

        boolean details = query.detailLevel() == GraduateProgramDetailLevel.DETAILS;
        String degreeClause = query.degreeTypes().isEmpty() ? "" : "AND dt.code IN (:degreeTypes)\n";
        String sql = details ? programDetailsSql(degreeClause) : programListSql(degreeClause);

        MapSqlParameterSource params = baseProgramParams(universityIds, query.degreeTypes());
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
                GROUP BY u.id, u.name, u.acronym, dt.code, gtr.currency
                ORDER BY u.name ASC, dt.code ASC NULLS LAST, gtr.currency ASC
                """.formatted(degreeClause);

        MapSqlParameterSource params = baseProgramParams(universityIds, query.degreeTypes());
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

    private MapSqlParameterSource baseProgramParams(List<Long> universityIds, List<String> degreeTypes) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("universityIds", universityIds);
        if (degreeTypes != null && !degreeTypes.isEmpty()) {
            params.addValue("degreeTypes", degreeTypes.stream()
                    .filter(StringUtils::hasText)
                    .map(value -> value.trim().toUpperCase(Locale.ROOT))
                    .toList());
        }
        return params;
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
                    COALESCE(admissions.admission_summary, 'Not available in official data') AS admission_summary,
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
                            COALESCE(gar.requirement_type, 'Not available in official data'),
                            LEFT(COALESCE(gar.requirement_text, 'Not available in official data'), 220)
                        ),
                        ' | ' ORDER BY gar.id ASC
                    ) AS admission_summary
                    FROM graduate_admission_requirement gar
                    WHERE gar.program_id = gp.id
                ) admissions ON TRUE
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

    private void appendProgramsSection(StringBuilder builder, List<ProgramRecord> programs, boolean details) {
        appendSectionTitle(builder, "Programs");
        if (programs.isEmpty()) {
            appendBullet(builder, "Result", "No matching official data found.");
            return;
        }

        int index = 1;
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

    private void appendTuitionSection(StringBuilder builder, List<TuitionAggregationRecord> aggregations) {
        appendSectionTitle(builder, "Tuition aggregation");
        if (aggregations.isEmpty()) {
            appendBullet(builder, "Result", "Average tuition is not computable from the official stored data.");
            return;
        }

        int index = 1;
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
                for (int i = degreeCursor; i < degreeEnd; i++) {
                    TuitionAggregationRecord aggregation = aggregations.get(i);
                    builder.append(index++).append(".\n");
                    appendIndentedBullet(builder, "Record count", String.valueOf(aggregation.recordCount()));
                    appendIndentedBullet(builder, "Numeric tuition records used", String.valueOf(aggregation.numericTuitionRecordsUsed()));
                    appendIndentedBullet(builder, "Currency", aggregation.currency());
                    appendIndentedBullet(builder, "Computed average", formatAverage(aggregation.averageTuition()));
                    appendIndentedBullet(builder, "Source URLs", aggregation.sourceUrls());
                }

                degreeCursor = degreeEnd;
            }

            cursor = groupEnd;
        }
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
        return sameText(previous.degreeTypeCode(), current.degreeTypeCode());
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

    private String buildProgramCitationId(ProgramRecord program, int index) {
        return "program-" + safeId(program.universityId()) + "-" + safeId(program.programId()) + "-" + index;
    }

    private String buildTuitionCitationId(TuitionAggregationRecord aggregation, int index) {
        return "tuition-" + safeId(aggregation.universityId()) + "-" + normalizeText(aggregation.degreeTypeCode()) + "-" + index;
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
        if (!StringUtils.hasText(aggregation.degreeTypeCode())
                || "Not available in official data".equalsIgnoreCase(aggregation.degreeTypeCode().trim())) {
            return university + " tuition";
        }
        return university + " " + aggregation.degreeTypeCode().trim() + " tuition";
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

    private String formatAverage(BigDecimal averageTuition) {
        if (averageTuition == null) {
            return "Average tuition is not computable from the official stored data.";
        }
        return averageTuition.setScale(2, RoundingMode.HALF_UP).toPlainString();
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
}
