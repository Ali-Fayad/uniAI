package com.uniai.chat.infrastructure.retrieval;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.domain.repository.UniversityCatalogRepository;
import com.uniai.chat.application.port.out.GraduateKnowledgeRetrievalPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SqlGraduateKnowledgeRetrievalAdapter implements GraduateKnowledgeRetrievalPort {

    private static final int MAX_RESULTS = 10;
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
    public String retrieveContext(String userMessage) {
        if (!StringUtils.hasText(userMessage)) {
            return "";
        }

        String normalizedMessage = userMessage.toLowerCase(Locale.ROOT);
        List<UniversityCatalog> matchedUniversities = findMatchingUniversities(normalizedMessage);
        Set<String> degreeTypes = detectDegreeTypes(normalizedMessage);

        if (matchedUniversities.isEmpty() && degreeTypes.isEmpty()) {
            return "";
        }

        List<ProgramRow> rows = queryPrograms(matchedUniversities, degreeTypes);
        if (rows.isEmpty()) {
            return "No matching official graduate data found.";
        }

        return formatContext(rows);
    }

    private List<UniversityCatalog> findMatchingUniversities(String normalizedMessage) {
        List<UniversityCatalog> allUniversities = universityCatalogRepository.findAll();
        if (allUniversities.isEmpty()) {
            return List.of();
        }

        List<UniversityCatalog> matches = new ArrayList<>();
        for (UniversityCatalog university : allUniversities) {
            if (university == null) {
                continue;
            }
            if (matchesUniversity(normalizedMessage, university)) {
                matches.add(university);
            }
        }
        return matches;
    }

    private boolean matchesUniversity(String normalizedMessage, UniversityCatalog university) {
        if (StringUtils.hasText(university.getAcronym())) {
            String acronym = university.getAcronym().toLowerCase(Locale.ROOT);
            if (containsWord(normalizedMessage, acronym)) {
                return true;
            }
        }

        if (StringUtils.hasText(university.getName())) {
            String name = university.getName().toLowerCase(Locale.ROOT);
            if (normalizedMessage.contains(name)) {
                return true;
            }

            for (String token : tokenize(name)) {
                if (token.length() > 2 && containsWord(normalizedMessage, token)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Set<String> detectDegreeTypes(String normalizedMessage) {
        Set<String> degreeTypes = new LinkedHashSet<>();

        if (normalizedMessage.contains("phd")
                || normalizedMessage.contains("doctor of philosophy")
                || normalizedMessage.contains("doctoral")
                || normalizedMessage.contains("doctorate")) {
            degreeTypes.add("PHD");
        }

        if (normalizedMessage.contains("master")
                || normalizedMessage.contains("masters")
                || normalizedMessage.contains("mba")
                || normalizedMessage.contains("m.a.")
                || normalizedMessage.contains("ma ")
                || normalizedMessage.contains(" m.a")
                || normalizedMessage.contains("m.sc")
                || normalizedMessage.contains("ms ")) {
            degreeTypes.add("MASTER");
        }

        return degreeTypes;
    }

    private List<ProgramRow> queryPrograms(List<UniversityCatalog> matchedUniversities, Set<String> degreeTypes) {
        List<Long> universityIds = matchedUniversities.stream()
                .map(UniversityCatalog::getId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        if (universityIds.isEmpty()) {
            return List.of();
        }

        String sql = """
                SELECT
                    gp.id AS program_id,
                    u.name AS university_name,
                    u.acronym AS university_acronym,
                    dt.code AS degree_type_code,
                    gp.official_degree_name,
                    gp.major_category,
                    gp.major,
                    fac.name AS faculty_name,
                    gp.credits,
                    gp.delivery_mode,
                    gp.thesis_or_non_thesis,
                    gp.program_description,
                    gp.official_program_url,
                    s.url AS source_url
                FROM graduate_program gp
                JOIN university u ON u.id = gp.university_id
                LEFT JOIN degree_type dt ON dt.id = gp.degree_type_id
                LEFT JOIN university_faculty fac ON fac.id = gp.faculty_id
                LEFT JOIN source s ON s.id = gp.source_id
                WHERE gp.university_id IN (:universityIds)
                %s
                ORDER BY u.name ASC, dt.code ASC NULLS LAST, gp.official_degree_name ASC
                LIMIT :limit
                """;

        String degreeClause = degreeTypes.isEmpty()
                ? ""
                : "AND dt.code IN (:degreeTypes)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("universityIds", universityIds)
                .addValue("limit", MAX_RESULTS);
        if (!degreeTypes.isEmpty()) {
            params.addValue("degreeTypes", degreeTypes);
        }

        return jdbcTemplate.query(
                String.format(sql, degreeClause),
                params,
                (rs, rowNum) -> new ProgramRow(
                        rs.getLong("program_id"),
                        rs.getString("university_name"),
                        rs.getString("university_acronym"),
                        rs.getString("degree_type_code"),
                        rs.getString("official_degree_name"),
                        rs.getString("major_category"),
                        rs.getString("major"),
                        rs.getString("faculty_name"),
                        rs.getObject("credits") != null ? rs.getInt("credits") : null,
                        rs.getString("delivery_mode"),
                        rs.getString("thesis_or_non_thesis"),
                        rs.getString("program_description"),
                        rs.getString("official_program_url"),
                        rs.getString("source_url")
                )
        );
    }

    private String formatContext(List<ProgramRow> rows) {
        StringBuilder builder = new StringBuilder();
        builder.append("Official graduate program context:\n");
        for (ProgramRow row : rows) {
            builder.append("- ");
            builder.append(safe(row.universityName()));
            if (StringUtils.hasText(row.universityAcronym())) {
                builder.append(" (").append(row.universityAcronym()).append(")");
            }
            builder.append(": ");
            builder.append(safe(row.officialDegreeName()));

            List<String> details = new ArrayList<>();
            if (StringUtils.hasText(row.degreeTypeCode())) {
                details.add(row.degreeTypeCode());
            }
            if (StringUtils.hasText(row.facultyName())) {
                details.add("faculty " + row.facultyName());
            }
            if (StringUtils.hasText(row.majorCategory())) {
                details.add("category " + row.majorCategory());
            }
            if (StringUtils.hasText(row.major())) {
                details.add("major " + row.major());
            }
            if (row.credits() != null) {
                details.add(row.credits() + " credits");
            }
            if (StringUtils.hasText(row.deliveryMode())) {
                details.add("delivery " + row.deliveryMode());
            }
            if (StringUtils.hasText(row.thesisOrNonThesis())) {
                details.add("thesis status " + row.thesisOrNonThesis());
            }
            if (StringUtils.hasText(row.programDescription())) {
                details.add("description: " + safe(row.programDescription()));
            }
            if (StringUtils.hasText(row.officialProgramUrl())) {
                details.add("program url: " + row.officialProgramUrl());
            }
            if (StringUtils.hasText(row.sourceUrl())) {
                details.add("source url: " + row.sourceUrl());
            }

            if (!details.isEmpty()) {
                builder.append(" [").append(String.join("; ", details)).append("]");
            }
            builder.append('\n');
        }
        return builder.toString().trim();
    }

    private String safe(String value) {
        if (!StringUtils.hasText(value)) {
            return "Unknown";
        }
        return value.trim();
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

    private record ProgramRow(
            Long programId,
            String universityName,
            String universityAcronym,
            String degreeTypeCode,
            String officialDegreeName,
            String majorCategory,
            String major,
            String facultyName,
            Integer credits,
            String deliveryMode,
            String thesisOrNonThesis,
            String programDescription,
            String officialProgramUrl,
            String sourceUrl
    ) {}
}
