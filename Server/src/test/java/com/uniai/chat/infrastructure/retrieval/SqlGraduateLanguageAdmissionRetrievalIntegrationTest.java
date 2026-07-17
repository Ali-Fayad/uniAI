package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.retrieval.GraduateKnowledgeFilters;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeOperation;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeResource;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import com.uniai.support.PostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "ai.provider=placeholder")
class SqlGraduateLanguageAdmissionRetrievalIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private SqlGraduateKnowledgeRetrievalAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long universityId;

    @BeforeEach
    void seedLanguageAndAdmissionRows() {
        universityId = jdbcTemplate.queryForObject(
                "INSERT INTO university (name, acronym, city) VALUES (?, ?, ?) RETURNING id",
                Long.class, "Language Admission Test University", "LATU", "Beirut");
        Long languageId = jdbcTemplate.queryForObject(
                "SELECT id FROM language WHERE LOWER(code) = 'en' ORDER BY id LIMIT 1", Long.class);
        Long degreeTypeId = jdbcTemplate.queryForObject(
                "SELECT id FROM degree_type WHERE code = 'MASTER' ORDER BY id LIMIT 1", Long.class);
        Long sourceId = jdbcTemplate.queryForObject("SELECT id FROM source ORDER BY id LIMIT 1", Long.class);
        Long programId = jdbcTemplate.queryForObject(
                "INSERT INTO graduate_program (university_id, degree_type_id, program_key, official_degree_name, primary_language_id, official_program_url, source_id) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id",
                Long.class, universityId, degreeTypeId, "latu-mba", "Master of Business Administration", languageId,
                "https://example.test/latu/mba", sourceId);
        jdbcTemplate.update(
                "INSERT INTO graduate_admission_requirement (university_id, program_id, scope_level, record_key, requirement_type, requirement_text, is_required, source_id) VALUES (?, ?, 'PROGRAM', ?, 'GMAT', ?, TRUE, ?)",
                universityId, programId, "latu-mba-gmat", "GMAT or equivalent entrance examination is required.", sourceId);
        jdbcTemplate.update(
                "INSERT INTO graduate_required_document (university_id, program_id, scope_level, record_key, document_type, document_name, is_optional, source_id) VALUES (?, ?, 'PROGRAM', ?, 'TRANSCRIPT', ?, FALSE, ?)",
                universityId, programId, "latu-mba-transcript", "Official university transcript", sourceId);
    }

    @AfterEach
    void removeRows() {
        jdbcTemplate.update("DELETE FROM university WHERE id = ?", universityId);
    }

    @Test
    void detailsUsePrimaryLanguageAndScopedAdmissionEvidence() {
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.PROGRAM_LOOKUP,
                GraduateKnowledgeResource.PROGRAM,
                GraduateKnowledgeOperation.DETAILS,
                new GraduateKnowledgeFilters(
                        List.of(new ResolvedUniversity(universityId, "Language Admission Test University", "LATU")),
                        List.of("MASTER"), List.of(), null, null, null,
                        List.of("English"), List.of("GMAT"), "Master of Business Administration"),
                GraduateProgramDetailLevel.DETAILS, false, false);

        String context = adapter.retrieveContext(query).formattedContext();

        assertTrue(context.contains("PROGRAM"), context);
        assertTrue(context.contains("GMAT"), context);
        assertTrue(context.contains("Official university transcript"), context);
    }
}
