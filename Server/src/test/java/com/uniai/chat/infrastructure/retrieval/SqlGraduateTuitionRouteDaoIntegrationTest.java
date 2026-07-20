package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.port.out.GraduateTuitionRouteDao;
import com.uniai.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "ai.provider=placeholder")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class SqlGraduateTuitionRouteDaoIntegrationTest extends PostgresIntegrationTest {
    @Autowired private SqlGraduateTuitionRouteDao dao;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void programTuitionIncludesProgramLinkedFacultyScopeRate() {
        Long aubId = jdbcTemplate.queryForObject("SELECT id FROM university WHERE acronym='AUB'", Long.class);

        GraduateTuitionRouteDao.TuitionPage page = dao.findTuition(criteria(
                List.of(aubId), "Computer Science", "MASTER", null, null,
                "2026-2027", "USD", "PER_CREDIT", null, 50));

        assertFalse(page.rows().isEmpty(), page.toString());
        assertTrue(page.rows().stream().anyMatch(row ->
                "FACULTY".equals(row.scopeLevel())
                        && new BigDecimal("1136.00").compareTo(row.amount()) == 0
                        && row.programName().contains("Computer Science")), page.toString());
    }

    @Test
    void explicitFacultyScopeCanBeFilteredWithoutRelabelingRows() {
        Long aubId = jdbcTemplate.queryForObject("SELECT id FROM university WHERE acronym='AUB'", Long.class);
        GraduateTuitionRouteDao.TuitionPage page = dao.findTuition(criteria(
                List.of(aubId), null, null, null, null, null, null, null, "FACULTY", 100));

        assertFalse(page.rows().isEmpty());
        assertTrue(page.rows().stream().allMatch(row -> "FACULTY".equals(row.scopeLevel())));
    }

    @Test
    void aggregationKeepsCurrencyBillingYearAndScopeGroupsSeparate() {
        Long aubId = jdbcTemplate.queryForObject("SELECT id FROM university WHERE acronym='AUB'", Long.class);
        List<GraduateTuitionRouteDao.TuitionAggregateRow> rows = dao.aggregateTuition(criteria(
                List.of(aubId), "Computer Science", "MASTER", null, null,
                null, null, null, null, 100));

        assertFalse(rows.isEmpty());
        assertTrue(rows.stream().anyMatch(row -> "USD".equals(row.currency())
                && "PER_CREDIT".equals(row.billingBasis()) && "FACULTY".equals(row.scopeLevel())));
        assertEquals(rows.size(), rows.stream()
                .map(row -> row.academicYear() + '|' + row.currency() + '|' + row.billingBasis() + '|' + row.scopeLevel())
                .distinct().count());
    }

    private GraduateTuitionRouteDao.TuitionCriteria criteria(
            List<Long> universities, String program, String degree, String faculty, String department,
            String year, String currency, String billing, String scope, int limit) {
        return new GraduateTuitionRouteDao.TuitionCriteria(
                universities, program, degree, faculty, department, year, currency, billing, scope, limit);
    }
}
