package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.port.out.GraduateCatalogRouteDao;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public final class SqlGraduateCatalogRouteDao implements GraduateCatalogRouteDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SqlGraduateCatalogRouteDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<UniversityRow> findUniversities(CatalogCriteria criteria) {
        Parts parts = universityFilters(criteria);
        parts.params.addValue("limit", criteria.limit());
        return jdbcTemplate.query("""
                SELECT DISTINCT u.id,u.name,u.acronym,u.name_ar,u.country
                FROM university u LEFT JOIN campus c ON c.university_id=u.id
                WHERE 1=1
                """ + parts.where + " ORDER BY u.name,u.id LIMIT :limit", parts.params,
                (rs,n) -> new UniversityRow(rs.getLong("id"),rs.getString("name"),rs.getString("acronym"),
                        rs.getString("name_ar"),rs.getString("country")));
    }

    @Override public long countUniversities(CatalogCriteria c) { return count("u.id", "FROM university u LEFT JOIN campus c ON c.university_id=u.id", universityFilters(c)); }

    @Override
    public List<CampusRow> findCampuses(CatalogCriteria criteria) {
        Parts parts = campusFilters(criteria);
        parts.params.addValue("limit", criteria.limit());
        return jdbcTemplate.query("""
                SELECT c.id,u.id AS university_id,u.name AS university_name,u.acronym AS university_acronym,
                       c.name,c.campus_type,c.city,c.locality,c.latitude,c.longitude
                FROM campus c JOIN university u ON u.id=c.university_id WHERE 1=1
                """ + parts.where + " ORDER BY LOWER(u.name),LOWER(c.city),LOWER(c.name),c.id LIMIT :limit",
                parts.params, (rs,n) -> new CampusRow(rs.getLong("id"),rs.getLong("university_id"),
                        rs.getString("university_name"),rs.getString("university_acronym"),rs.getString("name"),
                        rs.getString("campus_type"),rs.getString("city"),rs.getString("locality"),
                        rs.getBigDecimal("latitude"),rs.getBigDecimal("longitude")));
    }

    @Override public long countCampuses(CatalogCriteria c) { return count("c.id", "FROM campus c JOIN university u ON u.id=c.university_id", campusFilters(c)); }

    @Override
    public List<AcademicRow> findFaculties(CatalogCriteria criteria) {
        Parts parts = facultyFilters(criteria);
        parts.params.addValue("limit", criteria.limit());
        return jdbcTemplate.query("""
                SELECT f.id,u.id AS university_id,u.name AS university_name,u.acronym AS university_acronym,
                       NULL::bigint AS faculty_id,NULL::text AS faculty_name,f.name,f.short_name,
                       f.faculty_type AS type,f.official_url,f.notes
                FROM university_faculty f JOIN university u ON u.id=f.university_id WHERE 1=1
                """ + parts.where + " ORDER BY LOWER(u.name),LOWER(f.name),f.id LIMIT :limit", parts.params, this::academicRow);
    }

    @Override public long countFaculties(CatalogCriteria c) { return count("f.id", "FROM university_faculty f JOIN university u ON u.id=f.university_id", facultyFilters(c)); }

    @Override
    public List<AcademicRow> findDepartments(CatalogCriteria criteria) {
        Parts parts = departmentFilters(criteria);
        parts.params.addValue("limit", criteria.limit());
        return jdbcTemplate.query("""
                SELECT d.id,u.id AS university_id,u.name AS university_name,u.acronym AS university_acronym,
                       f.id AS faculty_id,f.name AS faculty_name,d.name,d.short_name,
                       'DEPARTMENT' AS type,d.official_url,d.notes
                FROM university_department d JOIN university u ON u.id=d.university_id
                LEFT JOIN university_faculty f ON f.id=d.faculty_id WHERE 1=1
                """ + parts.where + " ORDER BY LOWER(u.name),LOWER(d.name),d.id LIMIT :limit", parts.params, this::academicRow);
    }

    @Override public long countDepartments(CatalogCriteria c) { return count("d.id", "FROM university_department d JOIN university u ON u.id=d.university_id LEFT JOIN university_faculty f ON f.id=d.faculty_id", departmentFilters(c)); }

    @Override
    public List<UniversityStatisticsRow> universityStatistics(CatalogCriteria criteria) {
        Parts parts = universityFilters(criteria);
        parts.params.addValue("limit", criteria.limit());
        String sql = """
                SELECT u.id,u.name,u.acronym,
                       (SELECT COUNT(*) FROM campus c2 WHERE c2.university_id=u.id) AS campus_count,
                       (SELECT COUNT(*) FROM university_faculty f WHERE f.university_id=u.id) AS faculty_count,
                       (SELECT COUNT(*) FROM university_department d WHERE d.university_id=u.id) AS department_count,
                       (SELECT COUNT(*) FROM graduate_program gp WHERE gp.university_id=u.id) AS program_count
                FROM university u LEFT JOIN campus c ON c.university_id=u.id WHERE 1=1
                """ + parts.where + " GROUP BY u.id,u.name,u.acronym ORDER BY LOWER(u.name),u.id LIMIT :limit";
        return jdbcTemplate.query(sql, parts.params, (rs,n) -> new UniversityStatisticsRow(
                rs.getLong("id"),rs.getString("name"),rs.getString("acronym"),rs.getLong("campus_count"),
                rs.getLong("faculty_count"),rs.getLong("department_count"),rs.getLong("program_count")));
    }

    private Parts universityFilters(CatalogCriteria c) {
        Parts p = new Parts();
        ids(p,c,"u.id");
        eq(p,c.country(),"u.country","country");
        eq(p,c.city(),"c.city","city");
        search(p,c.searchQuery(),"CONCAT_WS(' ',u.name,u.acronym,u.name_ar)");
        return p;
    }

    private Parts campusFilters(CatalogCriteria c) {
        Parts p = new Parts();
        ids(p,c,"c.university_id");
        eq(p,c.city(),"c.city","city");
        eq(p,c.campusName(),"c.name","campusName");
        search(p,c.searchQuery(),"CONCAT_WS(' ',c.name,c.city,c.locality,u.name,u.acronym)");
        return p;
    }

    private Parts facultyFilters(CatalogCriteria c) {
        Parts p = new Parts(); ids(p,c,"f.university_id"); eq(p,c.facultyName(),"f.name","facultyName");
        search(p,c.searchQuery(),"CONCAT_WS(' ',f.name,f.short_name,u.name,u.acronym)"); return p;
    }

    private Parts departmentFilters(CatalogCriteria c) {
        Parts p = new Parts(); ids(p,c,"d.university_id"); eq(p,c.departmentName(),"d.name","departmentName");
        eq(p,c.facultyName(),"f.name","facultyName");
        search(p,c.searchQuery(),"CONCAT_WS(' ',d.name,d.short_name,f.name,u.name,u.acronym)"); return p;
    }

    private void ids(Parts p,CatalogCriteria c,String expression) {
        if (!c.universityIds().isEmpty()) { p.where.append(" AND ").append(expression).append(" IN (:universityIds)"); p.params.addValue("universityIds",c.universityIds()); }
    }
    private void eq(Parts p,String value,String expression,String parameter) {
        if (StringUtils.hasText(value)) { p.where.append(" AND LOWER(BTRIM(").append(expression).append("))=LOWER(BTRIM(:").append(parameter).append("))"); p.params.addValue(parameter,value); }
    }
    private void search(Parts p,String value,String expression) {
        if (StringUtils.hasText(value)) { p.where.append(" AND LOWER(").append(expression).append(") LIKE :search"); p.params.addValue("search","%"+value.trim().toLowerCase().replace("%","\\%").replace("_","\\_")+"%"); }
    }
    private long count(String id,String from,Parts p) {
        Long value=jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT "+id+") "+from+" WHERE 1=1 "+p.where,p.params,Long.class); return value==null?0:value;
    }
    private AcademicRow academicRow(java.sql.ResultSet rs,int n)throws java.sql.SQLException {
        return new AcademicRow(rs.getLong("id"),rs.getLong("university_id"),rs.getString("university_name"),
                rs.getString("university_acronym"),rs.getObject("faculty_id",Long.class),rs.getString("faculty_name"),
                rs.getString("name"),rs.getString("short_name"),rs.getString("type"),rs.getString("official_url"),rs.getString("notes"));
    }
    private static final class Parts { final StringBuilder where=new StringBuilder(); final MapSqlParameterSource params=new MapSqlParameterSource(); }
}
