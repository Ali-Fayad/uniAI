package com.uniai.chat.infrastructure.retrieval;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.domain.repository.UniversityCatalogRepository;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.port.out.GraduateKnowledgeRetrievalPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SqlGraduateKnowledgeRetrievalAdapter implements GraduateKnowledgeRetrievalPort {

    private static final int MAX_RESULTS = 10;
    private static final int MAX_TEXT_LENGTH = 220;
    private static final int MAX_CONVERSATION_WINDOW = 6;
    private static final Pattern WORD_SPLIT = Pattern.compile("[^A-Za-z0-9+]+");

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UniversityCatalogRepository universityCatalogRepository;

    public SqlGraduateKnowledgeRetrievalAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            UniversityCatalogRepository universityCatalogRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.universityCatalogRepository = universityCatalogRepository;
    }

    @Override
    public String retrieveContext(String userMessage, List<AiConversationMessage> recentConversationHistory) {
        if (!StringUtils.hasText(userMessage)) {
            return "";
        }

        QueryInterpretation interpretation = interpretQuery(userMessage, recentConversationHistory);
        if (interpretation.matchedUniversities().isEmpty() && interpretation.matchedDegreeTypes().isEmpty()) {
            return buildEmptyContext(interpretation, "No matching official data found.");
        }

        List<ProgramRecord> programs = queryPrograms(interpretation);
        if (programs.isEmpty()) {
            return buildEmptyContext(interpretation, "No matching official data found.");
        }

        List<TuitionAggregationRecord> tuitionAggregations = interpretation.tuitionAggregationIntent()
                ? queryTuitionAggregations(interpretation)
                : List.of();

        return buildStructuredContext(interpretation, programs, tuitionAggregations);
    }

    private QueryInterpretation interpretQuery(String userMessage, List<AiConversationMessage> recentConversationHistory) {
        String normalizedMessage = userMessage.toLowerCase(Locale.ROOT);
        List<RecentConversationCue> cues = extractRecentConversationCues(recentConversationHistory);

        List<UniversityCatalog> matchedUniversities = findMatchingUniversities(normalizedMessage, cues);
        Set<String> degreeTypes = detectDegreeTypes(normalizedMessage, cues);
        boolean tuitionAggregationIntent = detectTuitionAggregationIntent(normalizedMessage, cues);

        String focusDescription = buildFocusDescription(
                normalizedMessage,
                cues,
                matchedUniversities,
                degreeTypes,
                tuitionAggregationIntent
        );
        return new QueryInterpretation(userMessage, focusDescription, matchedUniversities, degreeTypes, tuitionAggregationIntent);
    }

    private boolean detectTuitionAggregationIntent(String normalizedMessage, List<RecentConversationCue> cues) {
        if (containsAny(normalizedMessage,
                "average tuition",
                "avg tuition",
                "mean tuition",
                "same tuition",
                "compare tuition",
                "tuition comparison",
                "tuition average",
                "tuition cost")) {
            return true;
        }

        if (normalizedMessage.contains("tuition")
                && containsAny(normalizedMessage, "average", "avg", "mean", "compare", "comparison")) {
            return true;
        }

        String recentTuitionCue = findMostRecentTuitionHint(cues);
        if (!StringUtils.hasText(recentTuitionCue)) {
            return false;
        }

        return containsAny(normalizedMessage,
                "same",
                "same at",
                "is it the same",
                "what about",
                "how about",
                "also",
                "compare",
                "comparison",
                "at ");
    }

    private List<RecentConversationCue> extractRecentConversationCues(List<AiConversationMessage> recentConversationHistory) {
        if (recentConversationHistory == null || recentConversationHistory.isEmpty()) {
            return List.of();
        }

        int startIndex = Math.max(0, recentConversationHistory.size() - MAX_CONVERSATION_WINDOW);
        List<RecentConversationCue> cues = new ArrayList<>();
        for (AiConversationMessage message : recentConversationHistory.subList(startIndex, recentConversationHistory.size())) {
            if (message == null || !StringUtils.hasText(message.getContent())) {
                continue;
            }
            cues.add(new RecentConversationCue(
                    safeRole(message.getRole()),
                    message.getContent().toLowerCase(Locale.ROOT)
            ));
        }
        return cues;
    }

    private List<UniversityCatalog> findMatchingUniversities(String normalizedMessage, List<RecentConversationCue> cues) {
        List<UniversityCatalog> allUniversities = universityCatalogRepository.findAll();
        if (allUniversities.isEmpty()) {
            return List.of();
        }

        String inheritedUniversityHint = findMostRecentUniversityHint(cues);
        List<UniversityCatalog> currentMatches = matchUniversities(normalizedMessage, allUniversities);
        if (!currentMatches.isEmpty()) {
            return currentMatches;
        }

        List<UniversityCatalog> matches = new ArrayList<>();
        for (UniversityCatalog university : allUniversities) {
            if (university == null) {
                continue;
            }
            if (matchesUniversity(inheritedUniversityHint, university)) {
                matches.add(university);
            }
        }
        return matches;
    }

    private List<UniversityCatalog> matchUniversities(String text, List<UniversityCatalog> universities) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        List<UniversityCatalog> matches = new ArrayList<>();
        for (UniversityCatalog university : universities) {
            if (university == null) {
                continue;
            }
            if (matchesUniversity(text, university)) {
                matches.add(university);
            }
        }
        return matches;
    }

    private boolean matchesUniversity(String text, UniversityCatalog university) {
        if (!StringUtils.hasText(text)) {
            return false;
        }

        String normalizedText = text.toLowerCase(Locale.ROOT);
        if (StringUtils.hasText(university.getAcronym())) {
            String acronym = university.getAcronym().toLowerCase(Locale.ROOT);
            if (containsWord(normalizedText, acronym)) {
                return true;
            }
        }

        if (StringUtils.hasText(university.getName())) {
            String name = university.getName().toLowerCase(Locale.ROOT);
            if (normalizedText.contains(name)) {
                return true;
            }

            for (String token : tokenize(name)) {
                if (token.length() > 2 && containsWord(normalizedText, token)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Set<String> detectDegreeTypes(String normalizedMessage, List<RecentConversationCue> cues) {
        Set<String> degreeTypes = new LinkedHashSet<>();
        String inheritedDegreeHint = findMostRecentDegreeHint(cues);

        if (matchesMaster(normalizedMessage)) {
            degreeTypes.add("MASTER");
        }
        if (matchesPhd(normalizedMessage)) {
            degreeTypes.add("PHD");
        }

        if (degreeTypes.isEmpty()) {
            if (matchesMaster(inheritedDegreeHint)) {
                degreeTypes.add("MASTER");
            }
            if (matchesPhd(inheritedDegreeHint)) {
                degreeTypes.add("PHD");
            }
        }

        return degreeTypes;
    }

    private boolean matchesMaster(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        String normalizedMessage = text.toLowerCase(Locale.ROOT);
        return normalizedMessage.contains("master")
                || normalizedMessage.contains("masters")
                || normalizedMessage.contains("master's")
                || normalizedMessage.contains("mba")
                || normalizedMessage.contains("m.a.")
                || normalizedMessage.contains("m.a ")
                || normalizedMessage.contains(" ma ")
                || normalizedMessage.contains("m.sc")
                || normalizedMessage.contains("ms ")
                || normalizedMessage.endsWith(" ms");
    }

    private boolean matchesPhd(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        String normalizedMessage = text.toLowerCase(Locale.ROOT);
        return normalizedMessage.contains("phd")
                || normalizedMessage.contains("doctor of philosophy")
                || normalizedMessage.contains("doctoral")
                || normalizedMessage.contains("doctorate");
    }

    private String findMostRecentUniversityHint(List<RecentConversationCue> cues) {
        for (int i = cues.size() - 1; i >= 0; i--) {
            RecentConversationCue cue = cues.get(i);
            String text = cue.content();
            if (containsAnyUniversityToken(text)) {
                return text;
            }
        }
        return "";
    }

    private String findMostRecentDegreeHint(List<RecentConversationCue> cues) {
        for (int i = cues.size() - 1; i >= 0; i--) {
            RecentConversationCue cue = cues.get(i);
            String text = cue.content();
            if (matchesMaster(text) || matchesPhd(text)) {
                return text;
            }
        }
        return "";
    }

    private String findMostRecentTuitionHint(List<RecentConversationCue> cues) {
        for (int i = cues.size() - 1; i >= 0; i--) {
            RecentConversationCue cue = cues.get(i);
            String text = cue.content();
            if (containsTuitionKeywords(text)) {
                return text;
            }
        }
        return "";
    }

    private boolean containsAnyUniversityToken(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        List<UniversityCatalog> universities = universityCatalogRepository.findAll();
        for (UniversityCatalog university : universities) {
            if (matchesUniversity(text, university)) {
                return true;
            }
        }
        return false;
    }

    private String buildFocusDescription(String normalizedMessage, List<RecentConversationCue> cues,
                                        List<UniversityCatalog> matchedUniversities, Set<String> degreeTypes,
                                        boolean tuitionAggregationIntent) {
        List<String> focusParts = new ArrayList<>();
        focusParts.add("current question: " + normalizedMessage);

        String inheritedUniversityHint = findMostRecentUniversityHint(cues);
        if (StringUtils.hasText(inheritedUniversityHint)) {
            focusParts.add("conversation university hint: " + inheritedUniversityHint);
        }
        String inheritedDegreeHint = findMostRecentDegreeHint(cues);
        if (StringUtils.hasText(inheritedDegreeHint)) {
            focusParts.add("conversation degree hint: " + inheritedDegreeHint);
        }
        if (!matchedUniversities.isEmpty()) {
            focusParts.add("matched universities: " + formatMatchedUniversities(matchedUniversities));
        }
        if (!degreeTypes.isEmpty()) {
            focusParts.add("matched degree type: " + String.join(" | ", degreeTypes));
        }
        if (tuitionAggregationIntent) {
            focusParts.add("tuition aggregation intent: detected");
        }

        return String.join(" ; ", focusParts);
    }

    private List<ProgramRecord> queryPrograms(QueryInterpretation interpretation) {
        List<Long> universityIds = resolveUniversityIds(interpretation);

        if (universityIds.isEmpty()) {
            return List.of();
        }

        String sql = """
                SELECT
                    gp.id AS program_id,
                    u.name AS university_name,
                    u.acronym AS university_acronym,
                    COALESCE(fac.name, 'Not available in official data') AS faculty_name,
                    COALESCE(l.name, 'Not available in official data') AS language_name,
                    COALESCE(dt.code, 'Not available in official data') AS degree_type_code,
                    COALESCE(gp.official_degree_name, 'Not available in official data') AS official_degree_name,
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
                        ' | ' ORDER BY gar.sort_order NULLS LAST, gar.id ASC
                    ) AS admission_summary
                    FROM graduate_admission_requirement gar
                    WHERE gar.program_id = gp.id
                ) admissions ON TRUE
                LEFT JOIN LATERAL (
                    SELECT STRING_AGG(DISTINCT s.url, ' | ' ORDER BY s.url) AS source_urls
                    FROM (
                        SELECT s.url
                        FROM source s
                        WHERE s.id = gp.source_id
                        UNION
                        SELECT s2.url
                        FROM graduate_program_source gps
                        JOIN source s2 ON s2.id = gps.source_id
                        WHERE gps.program_id = gp.id
                    ) s
                ) src ON TRUE
                WHERE gp.university_id IN (:universityIds)
                %s
                ORDER BY u.name ASC, dt.code ASC NULLS LAST, gp.official_degree_name ASC
                LIMIT :limit
                """;

        String degreeClause = interpretation.matchedDegreeTypes().isEmpty()
                ? ""
                : "AND dt.code IN (:degreeTypes)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("universityIds", universityIds)
                .addValue("limit", MAX_RESULTS);
        if (!interpretation.matchedDegreeTypes().isEmpty()) {
            params.addValue("degreeTypes", interpretation.matchedDegreeTypes());
        }

        return jdbcTemplate.query(
                String.format(sql, degreeClause),
                params,
                (rs, rowNum) -> new ProgramRecord(
                        rs.getLong("program_id"),
                        rs.getString("university_name"),
                        rs.getString("university_acronym"),
                        rs.getString("faculty_name"),
                        rs.getString("language_name"),
                        rs.getString("degree_type_code"),
                        rs.getString("official_degree_name"),
                        rs.getString("credits"),
                        rs.getString("delivery_mode"),
                        rs.getString("thesis_or_non_thesis"),
                        rs.getString("tuition_summary"),
                        rs.getString("admission_summary"),
                        rs.getString("official_program_url"),
                        rs.getString("source_urls")
                )
        );
    }

    private List<TuitionAggregationRecord> queryTuitionAggregations(QueryInterpretation interpretation) {
        List<Long> universityIds = resolveUniversityIds(interpretation);
        if (universityIds.isEmpty()) {
            return List.of();
        }

        String sql = """
                SELECT
                    gp.id AS program_id,
                    u.name AS university_name,
                    u.acronym AS university_acronym,
                    COALESCE(dt.code, 'Not available in official data') AS degree_type_code,
                    COALESCE(gp.official_degree_name, 'Not available in official data') AS official_degree_name,
                    gtr.amount AS amount,
                    COALESCE(gtr.currency, 'Not available in official data') AS currency,
                    COALESCE(gtr.academic_year, 'Not available in official data') AS academic_year,
                    COALESCE(gtr.category, 'Not available in official data') AS category,
                    COALESCE(gtr.billing_basis, 'Not available in official data') AS billing_basis,
                    COALESCE(gtr.notes, 'Not available in official data') AS notes,
                    COALESCE(gp.official_program_url, 'Not available in official data') AS official_program_url,
                    COALESCE(src.source_urls, 'Not available in official data') AS source_urls
                FROM graduate_tuition_rate gtr
                JOIN graduate_program gp ON gp.id = gtr.program_id
                JOIN university u ON u.id = gp.university_id
                LEFT JOIN degree_type dt ON dt.id = gp.degree_type_id
                LEFT JOIN LATERAL (
                    SELECT STRING_AGG(DISTINCT s.url, ' | ' ORDER BY s.url) AS source_urls
                    FROM (
                        SELECT s.url
                        FROM source s
                        WHERE s.id = gp.source_id
                        UNION
                        SELECT s2.url
                        FROM graduate_program_source gps
                        JOIN source s2 ON s2.id = gps.source_id
                        WHERE gps.program_id = gp.id
                    ) s
                ) src ON TRUE
                WHERE gp.university_id IN (:universityIds)
                %s
                ORDER BY u.name ASC, dt.code ASC NULLS LAST, gp.official_degree_name ASC, gtr.academic_year DESC NULLS LAST, gtr.id ASC
                LIMIT :limit
                """;

        String degreeClause = interpretation.matchedDegreeTypes().isEmpty()
                ? ""
                : "AND dt.code IN (:degreeTypes)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("universityIds", universityIds)
                .addValue("limit", 200);
        if (!interpretation.matchedDegreeTypes().isEmpty()) {
            params.addValue("degreeTypes", interpretation.matchedDegreeTypes());
        }

        List<TuitionRawRow> rows = jdbcTemplate.query(
                String.format(sql, degreeClause),
                params,
                (rs, rowNum) -> new TuitionRawRow(
                        rs.getLong("program_id"),
                        rs.getString("university_name"),
                        rs.getString("university_acronym"),
                        rs.getString("degree_type_code"),
                        rs.getString("official_degree_name"),
                        rs.getBigDecimal("amount"),
                        rs.getString("currency"),
                        rs.getString("academic_year"),
                        rs.getString("category"),
                        rs.getString("billing_basis"),
                        rs.getString("notes"),
                        rs.getString("official_program_url"),
                        rs.getString("source_urls")
                )
        );

        if (rows.isEmpty()) {
            return List.of();
        }

        Map<TuitionAggregationKey, TuitionAggregationBucket> buckets = new LinkedHashMap<>();
        for (TuitionRawRow row : rows) {
            String currencyKey = normalizeCurrency(row.currency());
            TuitionAggregationKey key = new TuitionAggregationKey(
                    row.universityName(),
                    row.universityAcronym(),
                    row.degreeTypeCode(),
                    currencyKey
            );

            TuitionAggregationBucket bucket = buckets.computeIfAbsent(key, ignored -> new TuitionAggregationBucket());
            bucket.programIds.add(row.programId());
            bucket.sourceUrls.addAll(splitSources(row.sourceUrls()));
            bucket.sourceUrls.addAll(splitSources(row.officialProgramUrl()));
            bucket.totalRows++;

            if (row.amount() == null || !StringUtils.hasText(currencyKey)
                    || "Not available in official data".equalsIgnoreCase(currencyKey)) {
                bucket.missingAmountRows++;
                if (!StringUtils.hasText(currencyKey) || "Not available in official data".equalsIgnoreCase(currencyKey)) {
                    bucket.missingCurrencyRows++;
                }
                continue;
            }

            bucket.numericRows++;
            bucket.sum = bucket.sum.add(row.amount());
        }

        List<TuitionAggregationRecord> records = new ArrayList<>();
        for (Map.Entry<TuitionAggregationKey, TuitionAggregationBucket> entry : buckets.entrySet()) {
            TuitionAggregationKey key = entry.getKey();
            TuitionAggregationBucket bucket = entry.getValue();

            BigDecimal average = null;
            if (bucket.numericRows > 0 && StringUtils.hasText(key.currency())
                    && !"Not available in official data".equalsIgnoreCase(key.currency())) {
                average = bucket.sum.divide(BigDecimal.valueOf(bucket.numericRows), 2, RoundingMode.HALF_UP);
            }

            records.add(new TuitionAggregationRecord(
                    key.universityName(),
                    key.universityAcronym(),
                    key.degreeTypeCode(),
                    key.currency(),
                    bucket.programIds.size(),
                    bucket.numericRows,
                    average,
                    buildTuitionExcludedSummary(bucket, average, key.currency()),
                    formatSources(bucket.sourceUrls)
            ));
        }

        records.sort(Comparator
                .comparing(TuitionAggregationRecord::numericTuitionRecordsUsed).reversed()
                .thenComparing(TuitionAggregationRecord::universityName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(TuitionAggregationRecord::degreeTypeCode, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(TuitionAggregationRecord::currency, String.CASE_INSENSITIVE_ORDER));

        return records.size() > MAX_RESULTS ? records.subList(0, MAX_RESULTS) : records;
    }

    private String buildStructuredContext(QueryInterpretation interpretation, List<ProgramRecord> programs,
                                          List<TuitionAggregationRecord> tuitionAggregations) {
        StringBuilder builder = new StringBuilder();
        appendSectionTitle(builder, "Query interpretation");
        appendBullet(builder, "User message", interpretation.userMessage());
        appendBullet(builder, "Recent conversation cue", interpretation.focusDescription());
        appendBullet(builder, "Matched universities", formatMatchedUniversities(interpretation.matchedUniversities()));
        appendBullet(builder, "Matched degree type", formatMatchedDegreeTypes(interpretation.matchedDegreeTypes()));

        if (interpretation.tuitionAggregationIntent()) {
            appendSectionTitle(builder, "Tuition aggregation");
            if (tuitionAggregations.isEmpty()) {
                appendBullet(builder, "Result", "Average tuition is not computable from the official stored data.");
            } else {
                int index = 1;
                for (TuitionAggregationRecord record : tuitionAggregations) {
                    builder.append(index++).append(".\n");
                    appendIndentedBullet(builder, "University", formatUniversity(record.universityName(), record.universityAcronym()));
                    appendIndentedBullet(builder, "Degree type", record.degreeTypeCode());
                    appendIndentedBullet(builder, "Programs considered", String.valueOf(record.programsConsidered()));
                    appendIndentedBullet(builder, "Numeric tuition records used", String.valueOf(record.numericTuitionRecordsUsed()));
                    appendIndentedBullet(builder, "Currency", record.currency());
                    appendIndentedBullet(builder, "Computed average", formatAverage(record.averageTuition()));
                    appendIndentedBullet(builder, "Excluded records reason summary", record.excludedRecordsReasonSummary());
                    appendIndentedBullet(builder, "Source URLs", record.sourceUrls());
                }
            }
        }

        appendSectionTitle(builder, "Programs");
        int index = 1;
        for (ProgramRecord program : programs) {
            builder.append(index++).append(".\n");
            appendIndentedBullet(builder, "University", formatUniversity(program));
            appendIndentedBullet(builder, "Faculty/school", program.facultyName());
            appendIndentedBullet(builder, "Program name", program.officialDegreeName());
            appendIndentedBullet(builder, "Degree type", program.degreeTypeCode());
            appendIndentedBullet(builder, "Language", program.languageName());
            appendIndentedBullet(builder, "Credits", program.credits());
            appendIndentedBullet(builder, "Delivery mode", program.deliveryMode());
            appendIndentedBullet(builder, "Thesis status", program.thesisOrNonThesis());
            appendIndentedBullet(builder, "Tuition summary", program.tuitionSummary());
            appendIndentedBullet(builder, "Admission summary", program.admissionSummary());
            appendIndentedBullet(builder, "Official source URL(s)", program.sourceUrls());
            appendIndentedBullet(builder, "Official program URL", program.officialProgramUrl());
        }

        appendSectionTitle(builder, "Sources");
        appendSourcesSection(builder, programs);

        appendSectionTitle(builder, "Missing/Unavailable data");
        appendMissingData(builder, interpretation, programs, tuitionAggregations);

        return builder.toString().trim();
    }

    private String buildEmptyContext(QueryInterpretation interpretation, String note) {
        StringBuilder builder = new StringBuilder();
        appendSectionTitle(builder, "Query interpretation");
        appendBullet(builder, "User message", interpretation.userMessage());
        appendBullet(builder, "Recent conversation cue", interpretation.focusDescription());
        appendBullet(builder, "Matched universities", formatMatchedUniversities(interpretation.matchedUniversities()));
        appendBullet(builder, "Matched degree type", formatMatchedDegreeTypes(interpretation.matchedDegreeTypes()));

        if (interpretation.tuitionAggregationIntent()) {
            appendSectionTitle(builder, "Tuition aggregation");
            appendBullet(builder, "Result", "Average tuition is not computable from the official stored data.");
        }

        appendSectionTitle(builder, "Programs");
        appendBullet(builder, "Result", note);

        appendSectionTitle(builder, "Sources");
        appendBullet(builder, "Result", "Not available in official data");

        appendSectionTitle(builder, "Missing/Unavailable data");
        appendBullet(builder, "Result", note);

        return builder.toString().trim();
    }

    private void appendSourcesSection(StringBuilder builder, List<ProgramRecord> programs) {
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

    private void appendMissingData(StringBuilder builder, QueryInterpretation interpretation, List<ProgramRecord> programs,
                                   List<TuitionAggregationRecord> tuitionAggregations) {
        Set<String> notes = new LinkedHashSet<>();
        if (interpretation.matchedUniversities().isEmpty()) {
            notes.add("Matched universities: Not available in official data");
        }
        if (interpretation.matchedDegreeTypes().isEmpty()) {
            notes.add("Matched degree type: Not available in official data");
        }
        if (interpretation.tuitionAggregationIntent() && tuitionAggregations.isEmpty()) {
            notes.add("Tuition aggregation: Average tuition is not computable from the official stored data.");
        }

        for (ProgramRecord program : programs) {
            addIfMissing(notes, "Faculty/school", program.facultyName());
            addIfMissing(notes, "Language", program.languageName());
            addIfMissing(notes, "Credits", program.credits());
            addIfMissing(notes, "Delivery mode", program.deliveryMode());
            addIfMissing(notes, "Thesis status", program.thesisOrNonThesis());
            addIfMissing(notes, "Tuition summary", program.tuitionSummary());
            addIfMissing(notes, "Admission summary", program.admissionSummary());
            addIfMissing(notes, "Official program URL", program.officialProgramUrl());
            addIfMissing(notes, "Official source URL(s)", program.sourceUrls());
        }

        for (TuitionAggregationRecord aggregation : tuitionAggregations) {
            addIfMissing(notes, "Tuition aggregation source URLs", aggregation.sourceUrls());
            if (!StringUtils.hasText(aggregation.averageTuitionText())) {
                notes.add("Tuition aggregation: Average tuition is not computable from the official stored data.");
            }
        }

        if (notes.isEmpty()) {
            appendBullet(builder, "Result", "Not available in official data");
            return;
        }

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

    private String formatUniversity(ProgramRecord program) {
        if (!StringUtils.hasText(program.universityName())) {
            return "Not available in official data";
        }
        if (!StringUtils.hasText(program.universityAcronym())) {
            return program.universityName();
        }
        return program.universityName() + " (" + program.universityAcronym() + ")";
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
        return averageTuition.toPlainString();
    }

    private List<Long> resolveUniversityIds(QueryInterpretation interpretation) {
        List<Long> matchedIds = interpretation.matchedUniversities().stream()
                .map(UniversityCatalog::getId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        if (!matchedIds.isEmpty()) {
            return matchedIds;
        }

        if (interpretation.tuitionAggregationIntent() && !interpretation.matchedDegreeTypes().isEmpty()) {
            return universityCatalogRepository.findAll().stream()
                    .map(UniversityCatalog::getId)
                    .filter(id -> id != null)
                    .distinct()
                    .toList();
        }

        return List.of();
    }

    private boolean containsTuitionKeywords(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        String normalizedText = text.toLowerCase(Locale.ROOT);
        return normalizedText.contains("tuition")
                || normalizedText.contains("fee")
                || normalizedText.contains("fees")
                || normalizedText.contains("cost");
    }

    private boolean containsAny(String text, String... phrases) {
        if (!StringUtils.hasText(text) || phrases == null) {
            return false;
        }
        String normalizedText = text.toLowerCase(Locale.ROOT);
        for (String phrase : phrases) {
            if (StringUtils.hasText(phrase) && normalizedText.contains(phrase.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private Set<String> splitSources(String value) {
        Set<String> sources = new LinkedHashSet<>();
        if (!StringUtils.hasText(value) || "Not available in official data".equalsIgnoreCase(value.trim())) {
            return sources;
        }
        for (String url : value.split("\\s*\\|\\s*")) {
            if (StringUtils.hasText(url)) {
                sources.add(url.trim());
            }
        }
        return sources;
    }

    private String formatSources(Set<String> sources) {
        if (sources.isEmpty()) {
            return "Not available in official data";
        }
        return String.join(" | ", sources);
    }

    private String normalizeCurrency(String currency) {
        if (!StringUtils.hasText(currency)) {
            return "Not available in official data";
        }
        return currency.trim();
    }

    private String buildTuitionExcludedSummary(TuitionAggregationBucket bucket, BigDecimal average, String currency) {
        List<String> reasons = new ArrayList<>();
        int missingAmount = Math.max(0, bucket.totalRows - bucket.numericRows);
        if (missingAmount > 0) {
            reasons.add(missingAmount + " records missing numeric amount");
        }
        if (!StringUtils.hasText(currency) || "Not available in official data".equalsIgnoreCase(currency.trim())) {
            reasons.add("currency not available in official data");
        }
        if (average == null) {
            reasons.add("average tuition is not computable from the official stored data");
        }
        if (reasons.isEmpty()) {
            return "None";
        }
        return String.join("; ", reasons);
    }

    private String formatMatchedUniversities(List<UniversityCatalog> universities) {
        if (universities.isEmpty()) {
            return "Not available in official data";
        }

        return universities.stream()
                .map(university -> formatUniversity(university.getName(), university.getAcronym()))
                .collect(Collectors.joining(" | "));
    }

    private String formatMatchedDegreeTypes(Set<String> degreeTypes) {
        if (degreeTypes.isEmpty()) {
            return "Not available in official data";
        }
        return String.join(" | ", degreeTypes);
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

    private boolean containsWord(String haystack, String needle) {
        if (!StringUtils.hasText(haystack) || !StringUtils.hasText(needle)) {
            return false;
        }
        String[] tokens = WORD_SPLIT.split(haystack);
        return Arrays.stream(tokens).anyMatch(token -> token.equalsIgnoreCase(needle));
    }

    private List<String> tokenize(String value) {
        return Arrays.stream(WORD_SPLIT.split(value))
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private String safeRole(String role) {
        if (!StringUtils.hasText(role)) {
            return "user";
        }
        String normalized = role.trim().toLowerCase(Locale.ROOT);
        if ("assistant".equals(normalized) || "model".equals(normalized)) {
            return "assistant";
        }
        return "user";
    }

    private record QueryInterpretation(
            String userMessage,
            String focusDescription,
            List<UniversityCatalog> matchedUniversities,
            Set<String> matchedDegreeTypes,
            boolean tuitionAggregationIntent
    ) {}

    private record RecentConversationCue(
            String role,
            String content
    ) {}

    private record ProgramRecord(
            Long programId,
            String universityName,
            String universityAcronym,
            String facultyName,
            String languageName,
            String degreeTypeCode,
            String officialDegreeName,
            String credits,
            String deliveryMode,
            String thesisOrNonThesis,
            String tuitionSummary,
            String admissionSummary,
            String officialProgramUrl,
            String sourceUrls
    ) {}

    private record TuitionRawRow(
            Long programId,
            String universityName,
            String universityAcronym,
            String degreeTypeCode,
            String officialDegreeName,
            BigDecimal amount,
            String currency,
            String academicYear,
            String category,
            String billingBasis,
            String notes,
            String officialProgramUrl,
            String sourceUrls
    ) {}

    private record TuitionAggregationKey(
            String universityName,
            String universityAcronym,
            String degreeTypeCode,
            String currency
    ) {}

    private static final class TuitionAggregationBucket {
        private final Set<Long> programIds = new LinkedHashSet<>();
        private final Set<String> sourceUrls = new LinkedHashSet<>();
        private int totalRows;
        private int numericRows;
        private int missingAmountRows;
        private int missingCurrencyRows;
        private BigDecimal sum = BigDecimal.ZERO;
    }

    private record TuitionAggregationRecord(
            String universityName,
            String universityAcronym,
            String degreeTypeCode,
            String currency,
            int programsConsidered,
            int numericTuitionRecordsUsed,
            BigDecimal averageTuition,
            String excludedRecordsReasonSummary,
            String sourceUrls
    ) {
        private String averageTuitionText() {
            return averageTuition == null ? "" : averageTuition.toPlainString();
        }
    }
}
