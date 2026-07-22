package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.port.out.GraduateProgramRouteDao;
import com.uniai.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "ai.provider=placeholder")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class SqlGraduateProgramRouteDaoIntegrationTest extends PostgresIntegrationTest {
    @Autowired
    private SqlGraduateProgramRouteDao dao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void aubProgramListIsCompleteAndIncludesComputerScience() {
        Map<String, Object> aub = jdbcTemplate.queryForMap(
                "SELECT id, name FROM university WHERE acronym = 'AUB'");
        long universityId = ((Number) aub.get("id")).longValue();
        long expected = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM graduate_program WHERE university_id = ?", Long.class, universityId);

        GraduateProgramRouteDao.ProgramPage page = dao.findPrograms(criteria(
                List.of(universityId), null, null, null, null, null, null, null, 200));

        assertEquals(expected, page.totalMatches());
        assertEquals(expected, page.rows().size());
        assertFalse(page.truncated());
        assertTrue(page.rows().stream().anyMatch(row ->
                contains(row.major(), "Computer Science") || contains(row.officialDegreeName(), "Computer Science")));
    }

    @Test
    void exactProgramLookupUsesMajorOfficialNameKeyAndAliases() {
        Long aubId = jdbcTemplate.queryForObject("SELECT id FROM university WHERE acronym = 'AUB'", Long.class);
        GraduateProgramRouteDao.ProgramPage byMajor = dao.findPrograms(criteria(
                List.of(aubId), null, "Computer Science", "MASTER", null, null, null, null, 20));
        assertTrue(byMajor.totalMatches() > 0, byMajor.toString());

        Map<String, Object> alias = jdbcTemplate.queryForMap(
                "SELECT gpa.university_id, gpa.alias FROM graduate_program_alias gpa ORDER BY gpa.id LIMIT 1");
        GraduateProgramRouteDao.ProgramPage byAlias = dao.findPrograms(criteria(
                List.of(((Number) alias.get("university_id")).longValue()), null,
                String.valueOf(alias.get("alias")), null, null, null, null, null, 20));
        assertTrue(byAlias.totalMatches() > 0, byAlias.toString());
    }

    @Test
    void groupedCountsEqualTheUntruncatedProgramTotal() {
        Long aubId = jdbcTemplate.queryForObject("SELECT id FROM university WHERE acronym = 'AUB'", Long.class);
        GraduateProgramRouteDao.ProgramCriteria criteria = criteria(
                List.of(aubId), null, null, null, null, null, null, null, 200);

        long total = dao.countPrograms(criteria);
        List<GraduateProgramRouteDao.GroupCountRow> groups = dao.countProgramsBy(
                criteria, GraduateProgramRouteDao.ProgramGrouping.UNIVERSITY);

        assertEquals(1, groups.size());
        assertEquals(total, groups.get(0).count());
    }

    @Test
    void degreeLevelAsTheFinalOptionalFilterDoesNotAttachOrderByToItsParameter() {
        Long maarefId = jdbcTemplate.queryForObject(
                "SELECT id FROM university WHERE LOWER(name) LIKE '%maaref%' LIMIT 1", Long.class);

        GraduateProgramRouteDao.ProgramPage page = dao.findPrograms(criteria(
                List.of(maarefId), null, null, "MASTER", null, null, null, null, 20));

        assertTrue(page.rows().stream().allMatch(row ->
                row.degreeType() == null || "MASTER".equalsIgnoreCase(row.degreeType())), page.toString());
    }

    private GraduateProgramRouteDao.ProgramCriteria criteria(
            List<Long> universityIds, String search, String program, String degree,
            String faculty, String department, String language, String city, int limit) {
        return new GraduateProgramRouteDao.ProgramCriteria(
                universityIds, search, program, degree, faculty, department, language, city, limit);
    }

    private boolean contains(String value, String expected) {
        return value != null && value.contains(expected);
    }
}
