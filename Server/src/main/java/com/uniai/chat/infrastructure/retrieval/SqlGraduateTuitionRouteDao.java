package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.port.out.GraduateTuitionRouteDao;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Verified parameterized SQL for route-based tuition and fee retrieval. */
@Component
public final class SqlGraduateTuitionRouteDao implements GraduateTuitionRouteDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SqlGraduateTuitionRouteDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public TuitionPage findTuition(TuitionCriteria criteria) {
        SqlParts filters = filters(criteria, "gtr");
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT gtr.id) " + tuitionFrom() + " WHERE 1=1 " + filters.where(),
                filters.parameters(), Long.class);
        String sql = """
                SELECT gtr.id AS tuition_id, u.id AS university_id, u.name AS university_name,
                       u.acronym AS university_acronym, gp.id AS program_id,
                       COALESCE(gp.official_degree_name, gp.major, gp.program_key) AS program_name,
                       dt.code AS degree_type, fac.name AS faculty_name, dep.name AS department_name,
                       gtr.scope_level, gtr.academic_year, gtr.currency, gtr.billing_basis,
                       gtr.amount, gtr.category, gtr.notes, s.title AS source_title, s.url AS source_url
                """ + tuitionFrom() + " WHERE 1=1 " + filters.where() + """
                ORDER BY LOWER(u.name), gtr.academic_year DESC, gtr.amount, gtr.id
                LIMIT :resultLimit
                """;
        filters.parameters().addValue("resultLimit", criteria.limit());
        List<TuitionRow> rows = jdbcTemplate.query(sql, filters.parameters(), this::mapTuition);
        return new TuitionPage(rows, total == null ? 0L : total);
    }

    @Override
    public List<TuitionAggregateRow> aggregateTuition(TuitionCriteria criteria) {
        SqlParts filters = filters(criteria, "gtr");
        String sql = """
                SELECT u.id AS university_id, u.name AS university_name, u.acronym AS university_acronym,
                       gtr.academic_year, gtr.currency, gtr.billing_basis, gtr.scope_level,
                       COUNT(gtr.amount) AS record_count, AVG(gtr.amount) AS average_amount,
                       MIN(gtr.amount) AS minimum_amount, MAX(gtr.amount) AS maximum_amount
                """ + tuitionFrom() + " WHERE 1=1 " + filters.where() + """
                GROUP BY u.id, u.name, u.acronym, gtr.academic_year, gtr.currency, gtr.billing_basis, gtr.scope_level
                ORDER BY LOWER(u.name), gtr.academic_year DESC, gtr.currency, gtr.billing_basis, gtr.scope_level
                LIMIT :resultLimit
                """;
        filters.parameters().addValue("resultLimit", criteria.limit());
        return jdbcTemplate.query(sql, filters.parameters(), (rs, n) -> new TuitionAggregateRow(
                rs.getLong("university_id"), rs.getString("university_name"),
                rs.getString("university_acronym"), rs.getString("academic_year"), rs.getString("currency"),
                rs.getString("billing_basis"), rs.getString("scope_level"), rs.getLong("record_count"),
                rs.getBigDecimal("average_amount"), rs.getBigDecimal("minimum_amount"), rs.getBigDecimal("maximum_amount")));
    }

    @Override
    public List<UniversityTuitionRankingRow> rankUniversitiesByTuition(TuitionRankingCriteria criteria) {
        RankingSqlParts filters = rankingFilters(criteria, "gtr", "gp", "fac", "dep");
        String order = "DESC".equalsIgnoreCase(criteria.order()) ? "DESC" : "ASC";
        String sql = """
                SELECT u.id AS university_id, u.name AS university_name, u.acronym AS university_acronym,
                       gtr.academic_year, gtr.currency, gtr.billing_basis, gtr.scope_level,
                       AVG(gtr.amount) AS average_amount, MIN(gtr.amount) AS minimum_amount,
                       MAX(gtr.amount) AS maximum_amount, COUNT(gtr.amount) AS matching_record_count
                FROM graduate_tuition_rate gtr
                JOIN graduate_program gp ON gp.id=gtr.program_id
                JOIN university u ON u.id=gtr.university_id
                LEFT JOIN degree_type dt ON dt.id=gp.degree_type_id
                LEFT JOIN university_faculty fac ON fac.id=COALESCE(gtr.faculty_id,gp.faculty_id)
                LEFT JOIN university_department dep ON dep.id=COALESCE(gtr.department_id,gp.department_id)
                WHERE 1=1
                """ + filters.where() + """
                GROUP BY u.id, u.name, u.acronym, gtr.academic_year, gtr.currency, gtr.billing_basis, gtr.scope_level
                """ + " ORDER BY average_amount " + order + ", LOWER(u.name), gtr.academic_year DESC LIMIT :resultLimit";
        filters.parameters().addValue("resultLimit", criteria.limit());
        return jdbcTemplate.query(sql, filters.parameters(), (rs, n) -> new UniversityTuitionRankingRow(
                rs.getLong("university_id"), rs.getString("university_name"), rs.getString("university_acronym"),
                rs.getString("academic_year"), rs.getString("currency"), rs.getString("billing_basis"), rs.getString("scope_level"),
                rs.getBigDecimal("average_amount"), rs.getBigDecimal("minimum_amount"),
                rs.getBigDecimal("maximum_amount"), rs.getLong("matching_record_count")));
    }

    @Override
    public List<ProgramTuitionRankingRow> rankProgramsByTuition(TuitionRankingCriteria criteria) {
        RankingSqlParts filters = rankingFilters(criteria, "gtr", "gp", "fac", "dep");
        String order = "DESC".equalsIgnoreCase(criteria.order()) ? "DESC" : "ASC";
        String sql = """
                SELECT gp.id AS program_id,
                       COALESCE(gp.official_degree_name, gp.major, gp.program_key) AS program_name,
                       u.id AS university_id, u.name AS university_name, u.acronym AS university_acronym,
                       gtr.academic_year, gtr.currency, gtr.billing_basis, gtr.scope_level,
                       AVG(gtr.amount) AS average_amount, MIN(gtr.amount) AS minimum_amount,
                       MAX(gtr.amount) AS maximum_amount, COUNT(gtr.amount) AS matching_record_count
                FROM graduate_tuition_rate gtr
                JOIN graduate_program gp ON gp.id=gtr.program_id
                JOIN university u ON u.id=gtr.university_id
                LEFT JOIN degree_type dt ON dt.id=gp.degree_type_id
                LEFT JOIN university_faculty fac ON fac.id=COALESCE(gtr.faculty_id,gp.faculty_id)
                LEFT JOIN university_department dep ON dep.id=COALESCE(gtr.department_id,gp.department_id)
                WHERE 1=1
                """ + filters.where() + """
                GROUP BY gp.id, gp.official_degree_name, gp.major, gp.program_key,
                         u.id, u.name, u.acronym, gtr.academic_year, gtr.currency, gtr.billing_basis, gtr.scope_level
                """ + " ORDER BY average_amount " + order + ", LOWER(u.name), LOWER(COALESCE(gp.official_degree_name, gp.major, gp.program_key)) LIMIT :resultLimit";
        filters.parameters().addValue("resultLimit", criteria.limit());
        return jdbcTemplate.query(sql, filters.parameters(), (rs, n) -> new ProgramTuitionRankingRow(
                rs.getLong("program_id"), rs.getString("program_name"), rs.getLong("university_id"),
                rs.getString("university_name"), rs.getString("university_acronym"),
                rs.getString("academic_year"), rs.getString("currency"), rs.getString("billing_basis"), rs.getString("scope_level"),
                rs.getBigDecimal("average_amount"), rs.getBigDecimal("minimum_amount"),
                rs.getBigDecimal("maximum_amount"), rs.getLong("matching_record_count")));
    }

    @Override
    public FeePage findFees(TuitionCriteria criteria) {
        SqlParts filters = filters(criteria, "gfi");
        String from = "FROM graduate_fee_item gfi JOIN university u ON u.id=gfi.university_id "
                + "LEFT JOIN graduate_program gp ON gp.id=gfi.program_id "
                + "LEFT JOIN degree_type dt ON dt.id=gp.degree_type_id "
                + "LEFT JOIN university_faculty fac ON fac.id=gfi.faculty_id "
                + "LEFT JOIN university_department dep ON dep.id=gfi.department_id "
                + "JOIN source s ON s.id=gfi.source_id ";
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT gfi.id) " + from + " WHERE 1=1 " + filters.where(),
                filters.parameters(), Long.class);
        String sql = """
                SELECT gfi.id AS fee_id, u.id AS university_id, u.name AS university_name,
                       COALESCE(gp.official_degree_name, gp.major, gp.program_key) AS program_name,
                       fac.name AS faculty_name, dep.name AS department_name, gfi.scope_level,
                       gfi.academic_year, gfi.fee_name, gfi.billing_basis, gfi.currency,
                       gfi.amount, gfi.category, gfi.notes, s.title AS source_title, s.url AS source_url
                """ + from + " WHERE 1=1 " + filters.where()
                + " ORDER BY LOWER(u.name), gfi.academic_year DESC NULLS LAST, LOWER(gfi.fee_name), gfi.id LIMIT :resultLimit";
        filters.parameters().addValue("resultLimit", criteria.limit());
        List<FeeRow> rows = jdbcTemplate.query(sql, filters.parameters(), this::mapFee);
        return new FeePage(rows, total == null ? 0L : total);
    }

    private String tuitionFrom() {
        return "FROM graduate_tuition_rate gtr JOIN university u ON u.id=gtr.university_id "
                + "LEFT JOIN graduate_program gp ON gp.id=gtr.program_id "
                + "LEFT JOIN degree_type dt ON dt.id=gp.degree_type_id "
                + "LEFT JOIN university_faculty fac ON fac.id=COALESCE(gtr.faculty_id,gp.faculty_id) "
                + "LEFT JOIN university_department dep ON dep.id=COALESCE(gtr.department_id,gp.department_id) "
                + "JOIN source s ON s.id=gtr.source_id ";
    }

    private SqlParts filters(TuitionCriteria criteria, String rateAlias) {
        StringBuilder where = new StringBuilder();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        if (!criteria.universityIds().isEmpty()) {
            where.append(" AND ").append(rateAlias).append(".university_id IN (:universityIds)");
            parameters.addValue("universityIds", criteria.universityIds());
        }
        if (StringUtils.hasText(criteria.programName())) {
            where.append("""
                     AND (LOWER(BTRIM(gp.official_degree_name)) = LOWER(BTRIM(:programName))
                       OR LOWER(BTRIM(gp.major)) = LOWER(BTRIM(:programName))
                       OR LOWER(BTRIM(gp.program_key)) = LOWER(BTRIM(:programName))
                       OR EXISTS (SELECT 1 FROM graduate_program_alias gpa
                                  WHERE gpa.program_id=gp.id AND LOWER(BTRIM(gpa.alias))=LOWER(BTRIM(:programName))))
                    """);
            parameters.addValue("programName", criteria.programName());
        }
        optionalEquals(where, parameters, criteria.degreeLevel(), "dt.code", "degreeLevel");
        optionalEquals(where, parameters, criteria.facultyName(), "fac.name", "facultyName");
        optionalEquals(where, parameters, criteria.departmentName(), "dep.name", "departmentName");
        optionalEquals(where, parameters, criteria.academicYear(), rateAlias + ".academic_year", "academicYear");
        optionalEquals(where, parameters, criteria.currency(), rateAlias + ".currency", "currency");
        optionalEquals(where, parameters, criteria.billingBasis(), rateAlias + ".billing_basis", "billingBasis");
        optionalEquals(where, parameters, criteria.scopeLevel(), rateAlias + ".scope_level", "scopeLevel");
        return new SqlParts(where.toString(), parameters);
    }

    private RankingSqlParts rankingFilters(TuitionRankingCriteria criteria, String rateAlias,
                                           String programAlias, String facultyAlias, String departmentAlias) {
        StringBuilder where = new StringBuilder();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        addIn(where, parameters, criteria.universityIds(), rateAlias + ".university_id", "universityIds");
        addProgramIn(where, parameters, criteria.programs(), programAlias);
        addIn(where, parameters, criteria.faculties(), "LOWER(BTRIM(" + facultyAlias + ".name))", "faculties");
        addIn(where, parameters, criteria.departments(), "LOWER(BTRIM(" + departmentAlias + ".name))", "departments");
        addIn(where, parameters, criteria.degreeTypes(), "LOWER(BTRIM(dt.code))", "degreeTypes");
        addIn(where, parameters, criteria.cities(),
                "EXISTS (SELECT 1 FROM campus c_rank WHERE c_rank.university_id=" + rateAlias + ".university_id " +
                        "AND LOWER(BTRIM(c_rank.city)) IN (:cities))", "cities");
        optionalEquals(where, parameters, criteria.academicYear(), rateAlias + ".academic_year", "academicYear");
        optionalEquals(where, parameters, criteria.currency(), rateAlias + ".currency", "currency");
        optionalEquals(where, parameters, criteria.billingBasis(), rateAlias + ".billing_basis", "billingBasis");
        return new RankingSqlParts(where.toString(), parameters);
    }

    private void addIn(StringBuilder where, MapSqlParameterSource parameters, List<?> values,
                       String expression, String parameter) {
        if (values == null || values.isEmpty()) return;
        if (expression.startsWith("EXISTS (")) {
            where.append(" AND ").append(expression);
            parameters.addValue(parameter, values.stream().map(String::valueOf).map(this::lower).toList());
            return;
        }
        where.append(" AND ").append(expression).append(" IN (:" ).append(parameter).append(")");
        parameters.addValue(parameter, values.stream().map(value -> value instanceof String s ? lower(s) : value).toList());
    }

    private void addProgramIn(StringBuilder where, MapSqlParameterSource parameters, List<String> values,
                              String programAlias) {
        if (values == null || values.isEmpty()) return;
        where.append(" AND (LOWER(BTRIM(COALESCE(").append(programAlias)
                .append(".official_degree_name,").append(programAlias).append(".major,")
                .append(programAlias).append(".program_key))) IN (:programs)")
                .append(" OR EXISTS (SELECT 1 FROM graduate_program_alias gpa_rank")
                .append(" WHERE gpa_rank.program_id=").append(programAlias).append(".id")
                .append(" AND LOWER(BTRIM(gpa_rank.alias)) IN (:programs)))");
        parameters.addValue("programs", values.stream().map(this::lower).toList());
    }

    private String lower(String value) { return value == null ? null : value.trim().toLowerCase(java.util.Locale.ROOT); }

    private void optionalEquals(StringBuilder where, MapSqlParameterSource parameters,
                                String value, String expression, String parameter) {
        if (!StringUtils.hasText(value)) return;
        where.append(" AND LOWER(BTRIM(").append(expression).append("))=LOWER(BTRIM(:")
                .append(parameter).append("))");
        parameters.addValue(parameter, value);
    }

    private TuitionRow mapTuition(ResultSet rs, int rowNum) throws SQLException {
        return new TuitionRow(rs.getLong("tuition_id"), rs.getLong("university_id"),
                rs.getString("university_name"), rs.getString("university_acronym"),
                rs.getObject("program_id", Long.class), rs.getString("program_name"), rs.getString("degree_type"),
                rs.getString("faculty_name"), rs.getString("department_name"), rs.getString("scope_level"),
                rs.getString("academic_year"), rs.getString("currency"), rs.getString("billing_basis"),
                rs.getBigDecimal("amount"), rs.getString("category"), rs.getString("notes"),
                rs.getString("source_title"), rs.getString("source_url"));
    }

    private FeeRow mapFee(ResultSet rs, int rowNum) throws SQLException {
        return new FeeRow(rs.getLong("fee_id"), rs.getLong("university_id"), rs.getString("university_name"),
                rs.getString("program_name"), rs.getString("faculty_name"), rs.getString("department_name"),
                rs.getString("scope_level"), rs.getString("academic_year"), rs.getString("fee_name"),
                rs.getString("billing_basis"), rs.getString("currency"), rs.getBigDecimal("amount"),
                rs.getString("category"), rs.getString("notes"), rs.getString("source_title"), rs.getString("source_url"));
    }

    private record SqlParts(String where, MapSqlParameterSource parameters) {}
    private record RankingSqlParts(String where, MapSqlParameterSource parameters) {}
}
