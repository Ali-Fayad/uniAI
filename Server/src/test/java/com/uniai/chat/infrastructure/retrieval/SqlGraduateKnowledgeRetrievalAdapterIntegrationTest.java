package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.citation.GraduateKnowledgeRetrievalResult;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import com.uniai.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "ai.provider=placeholder")
class SqlGraduateKnowledgeRetrievalAdapterIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private SqlGraduateKnowledgeRetrievalAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ProgramFixture programFixture;

    @BeforeEach
    void setUp() {
        programFixture = discoverProgramFixture();
        assertNotNull(programFixture);
    }

    @Test
    void retrieveContextShouldReturnStructuredProgramContextAndCitationsFromRealDatabase() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                List.of(new ResolvedUniversity(
                        programFixture.universityId(),
                        programFixture.universityName(),
                        programFixture.universityAcronym()
                )),
                List.of(programFixture.degreeTypeCode()),
                GraduateProgramDetailLevel.DETAILS,
                false,
                false
        );

        GraduateKnowledgeRetrievalResult result = adapter.retrieveContext(query);

        assertTrue(result.formattedContext().contains(programFixture.universityName()), result.formattedContext());
        assertTrue(result.formattedContext().contains(programFixture.officialProgramName()), result.formattedContext());
        assertTrue(result.formattedContext().contains("Sources:"), result.formattedContext());
        assertFalse(result.citations().isEmpty());
        assertEquals("S1", result.citations().get(0).label());
        assertEquals(programFixture.universityId(), result.citations().get(0).universityId());
    }

    @Test
    void retrieveContextShouldReturnTuitionAggregationContextFromRealDatabase() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.TUITION_AGGREGATION,
                List.of(new ResolvedUniversity(
                        programFixture.universityId(),
                        programFixture.universityName(),
                        programFixture.universityAcronym()
                )),
                List.of(programFixture.degreeTypeCode()),
                null,
                false,
                false
        );

        GraduateKnowledgeRetrievalResult result = adapter.retrieveContext(query);

        assertTrue(result.formattedContext().contains("Tuition aggregation:"), result.formattedContext());
        assertTrue(result.formattedContext().contains(programFixture.universityName()), result.formattedContext());
        assertTrue(result.formattedContext().contains(programFixture.currency()), result.formattedContext());
        assertFalse(result.citations().isEmpty());
        assertEquals("S1", result.citations().get(0).label());
    }

    private ProgramFixture discoverProgramFixture() {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT gp.id AS program_id,
                       gp.university_id,
                       u.name AS university_name,
                       u.acronym AS university_acronym,
                       gp.official_degree_name,
                       dt.code AS degree_type_code
                FROM graduate_program gp
                JOIN graduate_tuition_rate gtr ON gtr.program_id = gp.id AND gtr.amount IS NOT NULL
                JOIN university u ON u.id = gp.university_id
                JOIN degree_type dt ON dt.id = gp.degree_type_id
                WHERE gp.official_degree_name IS NOT NULL
                  AND dt.code IS NOT NULL
                ORDER BY gp.university_id, gp.id
                LIMIT 1
                """);

        Map<String, Object> tuitionRow = jdbcTemplate.queryForMap("""
                SELECT gtr.currency
                FROM graduate_tuition_rate gtr
                WHERE gtr.program_id = ?
                  AND gtr.amount IS NOT NULL
                ORDER BY gtr.id
                LIMIT 1
                """, row.get("program_id"));

        return new ProgramFixture(
                toLong(row.get("university_id")),
                stringValue(row.get("university_name")),
                stringValue(row.get("university_acronym")),
                stringValue(row.get("official_degree_name")),
                stringValue(row.get("degree_type_code")),
                stringValue(tuitionRow.get("currency"))
        );
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? null : Long.valueOf(value.toString());
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private record ProgramFixture(
            Long universityId,
            String universityName,
            String universityAcronym,
            String officialProgramName,
            String degreeTypeCode,
            String currency
    ) {
    }
}
