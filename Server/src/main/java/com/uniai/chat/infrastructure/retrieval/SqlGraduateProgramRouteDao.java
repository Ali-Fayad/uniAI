package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.port.out.GraduateProgramRouteDao;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Verified parameterized SQL for the route-based program planner. */
@Component
public final class SqlGraduateProgramRouteDao implements GraduateProgramRouteDao {

    private static final String PROGRAM_NAME_EXPRESSION = "COALESCE(gp.official_degree_name, gp.major, gp.program_key)";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SqlGraduateProgramRouteDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ProgramPage findPrograms(ProgramCriteria criteria) {
        SqlParts parts = filters(criteria);
        long total = count(parts);

        String sql = """
                SELECT gp.id AS program_id,
                       u.id AS university_id,
                       u.name AS university_name,
                       u.acronym AS university_acronym,
                       gp.program_key,
                       gp.major,
                       gp.official_degree_name,
                       dt.code AS degree_type,
                       fac.name AS faculty_name,
                       dep.name AS department_name,
                       gp.credits,
                       CASE
                           WHEN gp.duration_value IS NULL THEN NULL
                           ELSE CONCAT(
                               TRIM(TRAILING '.0' FROM gp.duration_value::text),
                               ' ',
                               gp.duration_unit
                           )
                       END AS duration,
                       lang.name AS language,
                       gp.delivery_mode,
                       gp.thesis_or_non_thesis,
                       gp.program_description,
                       gp.official_program_url,
                       s.title AS source_title,
                       s.url AS source_url
                FROM graduate_program gp
                JOIN university u
                  ON u.id = gp.university_id
                LEFT JOIN degree_type dt
                  ON dt.id = gp.degree_type_id
                LEFT JOIN university_faculty fac
                  ON fac.id = gp.faculty_id
                LEFT JOIN university_department dep
                  ON dep.id = gp.department_id
                LEFT JOIN language lang
                  ON lang.id = gp.primary_language_id
                JOIN source s
                  ON s.id = gp.source_id
                WHERE 1 = 1
                """ + parts.where() + " \n" + """
                ORDER BY
                    LOWER(u.name),
                    LOWER(COALESCE(gp.official_degree_name, gp.major, gp.program_key)),
                    gp.id
                LIMIT :resultLimit
                """;

        parts.parameters().addValue("resultLimit", criteria.limit());

        List<ProgramRow> rows = jdbcTemplate.query(
                sql,
                parts.parameters(),
                this::mapProgram);

        return new ProgramPage(rows, total);
    }

    @Override
    public long countPrograms(ProgramCriteria criteria) {
        return count(filters(criteria));
    }

    @Override
    public List<GroupCountRow> countProgramsBy(
            ProgramCriteria criteria,
            ProgramGrouping grouping) {
        SqlParts parts = filters(criteria);

        String id;
        String name;
        String additionalFilter = "";

        switch (grouping) {
            case UNIVERSITY -> {
                id = "u.id";
                name = "u.name";
            }
            case FACULTY -> {
                id = "fac.id";
                name = "fac.name";
                additionalFilter = " AND fac.id IS NOT NULL";
            }
            case DEPARTMENT -> {
                id = "dep.id";
                name = "dep.name";
                additionalFilter = " AND dep.id IS NOT NULL";
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported program grouping: " + grouping);
        }

        String sql = "SELECT "
                + id + " AS group_id, "
                + name + " AS group_name, "
                + "u.name AS university_name, "
                + "COUNT(DISTINCT gp.id) AS result_count "
                + baseFrom()
                + " WHERE 1 = 1 "
                + parts.where()
                + additionalFilter
                + " GROUP BY "
                + id + ", "
                + name + ", "
                + "u.id, "
                + "u.name"
                + " ORDER BY result_count DESC, LOWER(" + name + ")"
                + " LIMIT :resultLimit";

        parts.parameters().addValue("resultLimit", criteria.limit());

        return jdbcTemplate.query(
                sql,
                parts.parameters(),
                (rs, rowNum) -> new GroupCountRow(
                        rs.getObject("group_id", Long.class),
                        rs.getString("group_name"),
                        rs.getString("university_name"),
                        rs.getLong("result_count")));
    }

    @Override
    public List<ProgramEvidenceRow> findTracks(ProgramCriteria criteria) {
        SqlParts parts = filters(criteria);

        String sql = """
                SELECT gp.id AS program_id,
                       u.id AS university_id,
                       u.name AS university_name,
                       COALESCE(
                           gp.official_degree_name,
                           gp.major,
                           gp.program_key
                       ) AS program_name,
                       gpt.track_type AS kind,
                       gpt.track_name AS value,
                       gpt.description AS details,
                       s.title AS source_title,
                       s.url AS source_url
                """
                + baseFrom()
                + """
                         JOIN graduate_program_track gpt
                           ON gpt.program_id = gp.id
                         JOIN source s
                           ON s.id = gpt.source_id
                         WHERE 1 = 1
                        """
                + parts.where()
                + """
                         ORDER BY
                             LOWER(u.name),
                             LOWER(COALESCE(
                                 gp.official_degree_name,
                                 gp.major,
                                 gp.program_key
                             )),
                             gpt.track_order NULLS LAST,
                             LOWER(gpt.track_name)
                         LIMIT :resultLimit
                        """;

        parts.parameters().addValue("resultLimit", criteria.limit());

        return jdbcTemplate.query(
                sql,
                parts.parameters(),
                this::mapEvidence);
    }

    @Override
    public List<ProgramEvidenceRow> findLanguages(ProgramCriteria criteria) {
        SqlParts parts = filters(criteria);

        String sql = """
                SELECT gp.id AS program_id,
                       u.id AS university_id,
                       u.name AS university_name,
                       COALESCE(
                           gp.official_degree_name,
                           gp.major,
                           gp.program_key
                       ) AS program_name,
                       'PRIMARY_LANGUAGE' AS kind,
                       lang.name AS value,
                       NULL AS details,
                       s.title AS source_title,
                       s.url AS source_url
                """
                + baseFrom()
                + """
                         JOIN language lang
                           ON lang.id = gp.primary_language_id
                         JOIN source s
                           ON s.id = gp.source_id
                         WHERE 1 = 1
                        """
                + parts.where()
                + """
                         ORDER BY
                             LOWER(u.name),
                             LOWER(COALESCE(
                                 gp.official_degree_name,
                                 gp.major,
                                 gp.program_key
                             )),
                             LOWER(lang.name)
                         LIMIT :resultLimit
                        """;

        parts.parameters().addValue("resultLimit", criteria.limit());

        return jdbcTemplate.query(
                sql,
                parts.parameters(),
                this::mapEvidence);
    }

    @Override
    public List<ProgramEvidenceRow> findSources(ProgramCriteria criteria) {
        SqlParts parts = filters(criteria);

        String sql = """
                SELECT gp.id AS program_id,
                       u.id AS university_id,
                       u.name AS university_name,
                       COALESCE(
                           gp.official_degree_name,
                           gp.major,
                           gp.program_key
                       ) AS program_name,
                       gps.source_role AS kind,
                       s.title AS value,
                       gps.evidence_text AS details,
                       s.title AS source_title,
                       s.url AS source_url
                """
                + baseFrom()
                + """
                         JOIN graduate_program_source gps
                           ON gps.program_id = gp.id
                         JOIN source s
                           ON s.id = gps.source_id
                         WHERE 1 = 1
                        """
                + parts.where()
                + """
                         ORDER BY
                             LOWER(u.name),
                             LOWER(COALESCE(
                                 gp.official_degree_name,
                                 gp.major,
                                 gp.program_key
                             )),
                             gps.source_order NULLS LAST,
                             s.id
                         LIMIT :resultLimit
                        """;

        parts.parameters().addValue("resultLimit", criteria.limit());

        return jdbcTemplate.query(
                sql,
                parts.parameters(),
                this::mapEvidence);
    }

    private long count(SqlParts parts) {
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT gp.id) "
                        + baseFrom()
                        + " WHERE 1 = 1 "
                        + parts.where(),
                parts.parameters(),
                Long.class);

        return total == null ? 0L : total;
    }

    private String baseFrom() {
        return """
                FROM graduate_program gp
                JOIN university u
                  ON u.id = gp.university_id
                LEFT JOIN degree_type dt
                  ON dt.id = gp.degree_type_id
                LEFT JOIN university_faculty fac
                  ON fac.id = gp.faculty_id
                LEFT JOIN university_department dep
                  ON dep.id = gp.department_id
                """;
    }

    private SqlParts filters(ProgramCriteria criteria) {
        StringBuilder where = new StringBuilder();
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        if (!criteria.universityIds().isEmpty()) {
            where.append(" AND gp.university_id IN (:universityIds)");
            parameters.addValue("universityIds", criteria.universityIds());
        }

        if (StringUtils.hasText(criteria.degreeLevel())) {
            where.append(" AND dt.code = :degreeLevel");
            parameters.addValue("degreeLevel", criteria.degreeLevel());
        }

        if (StringUtils.hasText(criteria.facultyName())) {
            where.append("""
                     AND LOWER(BTRIM(fac.name)) =
                         LOWER(BTRIM(:facultyName))
                    """);
            parameters.addValue("facultyName", criteria.facultyName());
        }

        if (StringUtils.hasText(criteria.departmentName())) {
            where.append("""
                     AND LOWER(BTRIM(dep.name)) =
                         LOWER(BTRIM(:departmentName))
                    """);
            parameters.addValue("departmentName", criteria.departmentName());
        }

        if (StringUtils.hasText(criteria.language())) {
            where.append("""
                     AND EXISTS (
                         SELECT 1
                         FROM language l
                         WHERE l.id = gp.primary_language_id
                           AND LOWER(BTRIM(l.name)) =
                               LOWER(BTRIM(:language))
                     )
                    """);
            parameters.addValue("language", criteria.language());
        }

        if (StringUtils.hasText(criteria.city())) {
            where.append("""
                     AND EXISTS (
                         SELECT 1
                         FROM campus c
                         WHERE c.university_id = gp.university_id
                           AND LOWER(BTRIM(c.city)) =
                               LOWER(BTRIM(:city))
                     )
                    """);
            parameters.addValue("city", criteria.city());
        }

        if (StringUtils.hasText(criteria.programName())) {
            where.append("""
                     AND (
                         LOWER(BTRIM(gp.official_degree_name)) =
                             LOWER(BTRIM(:programName))
                         OR LOWER(BTRIM(gp.major)) =
                             LOWER(BTRIM(:programName))
                         OR LOWER(BTRIM(gp.program_key)) =
                             LOWER(BTRIM(:programName))
                         OR EXISTS (
                             SELECT 1
                             FROM graduate_program_alias gpa
                             WHERE gpa.program_id = gp.id
                               AND LOWER(BTRIM(gpa.alias)) =
                                   LOWER(BTRIM(:programName))
                         )
                     )
                    """);
            parameters.addValue("programName", criteria.programName());
        }

        if (StringUtils.hasText(criteria.searchQuery())) {
            where.append("""
                     AND (
                         LOWER(COALESCE(gp.official_degree_name, ''))
                             LIKE :searchPattern ESCAPE '\\'
                         OR LOWER(COALESCE(gp.major, ''))
                             LIKE :searchPattern ESCAPE '\\'
                         OR LOWER(COALESCE(gp.major_category, ''))
                             LIKE :searchPattern ESCAPE '\\'
                         OR LOWER(gp.program_key)
                             LIKE :searchPattern ESCAPE '\\'
                         OR EXISTS (
                             SELECT 1
                             FROM graduate_program_alias gpa
                             WHERE gpa.program_id = gp.id
                               AND LOWER(gpa.alias)
                                   LIKE :searchPattern ESCAPE '\\'
                         )
                     )
                    """);

            parameters.addValue(
                    "searchPattern",
                    "%" + escapeLike(
                            criteria.searchQuery().trim().toLowerCase()) + "%");
        }

        return new SqlParts(where.toString(), parameters);
    }

    private String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private ProgramRow mapProgram(
            ResultSet rs,
            int rowNum) throws SQLException {
        return new ProgramRow(
                rs.getLong("program_id"),
                rs.getLong("university_id"),
                rs.getString("university_name"),
                rs.getString("university_acronym"),
                rs.getString("program_key"),
                rs.getString("major"),
                rs.getString("official_degree_name"),
                rs.getString("degree_type"),
                rs.getString("faculty_name"),
                rs.getString("department_name"),
                rs.getObject("credits", Integer.class),
                rs.getString("duration"),
                rs.getString("language"),
                rs.getString("delivery_mode"),
                rs.getString("thesis_or_non_thesis"),
                rs.getString("program_description"),
                rs.getString("official_program_url"),
                rs.getString("source_title"),
                rs.getString("source_url"));
    }

    private ProgramEvidenceRow mapEvidence(
            ResultSet rs,
            int rowNum) throws SQLException {
        return new ProgramEvidenceRow(
                rs.getLong("program_id"),
                rs.getLong("university_id"),
                rs.getString("university_name"),
                rs.getString("program_name"),
                rs.getString("kind"),
                rs.getString("value"),
                rs.getString("details"),
                rs.getString("source_title"),
                rs.getString("source_url"));
    }

    private record SqlParts(
            String where,
            MapSqlParameterSource parameters) {
    }
}
