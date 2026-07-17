package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.retrieval.GraduateKnowledgeFilters;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeOperation;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeResource;
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
class SqlGraduateAcademicStructureRetrievalAdapterIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private SqlGraduateKnowledgeRetrievalAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long universityId;

    @BeforeEach
    void seedStructuredAcademicRows() {
        universityId = jdbcTemplate.queryForObject(
                "INSERT INTO university (name, acronym, city) VALUES (?, ?, ?) RETURNING id",
                Long.class, "Academic Structure Test University", "ASTU", "Beirut");
        Long facultyId = jdbcTemplate.queryForObject(
                "INSERT INTO university_faculty (university_id, name, official_url) VALUES (?, ?, ?) RETURNING id",
                Long.class, universityId, "Faculty of Engineering", "https://example.test/faculty");
        jdbcTemplate.update(
                "INSERT INTO university_department (university_id, faculty_id, name, official_url) VALUES (?, ?, ?, ?)",
                universityId, facultyId, "Computer Science", "https://example.test/department");
        Long departmentId = jdbcTemplate.queryForObject(
                "SELECT id FROM university_department WHERE university_id = ? AND name = ?",
                Long.class, universityId, "Computer Science");
        Long degreeTypeId = jdbcTemplate.queryForObject("SELECT id FROM degree_type WHERE code = 'MASTER' LIMIT 1", Long.class);
        Long sourceId = jdbcTemplate.queryForObject("SELECT id FROM source ORDER BY id LIMIT 1", Long.class);
        jdbcTemplate.update(
                "INSERT INTO graduate_program (university_id, faculty_id, department_id, degree_type_id, program_key, official_degree_name, official_program_url, source_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                universityId, facultyId, departmentId, degreeTypeId, "astu-computer-science", "Master of Computer Science", "https://example.test/program", sourceId);
    }

    @AfterEach
    void removeStructuredAcademicRows() {
        jdbcTemplate.update("DELETE FROM university WHERE id = ?", universityId);
    }

    @Test
    void facultyCountUsesStructuredDistinctRows() {
        GraduateKnowledgeQuery query = query(GraduateKnowledgeResource.FACULTY, GraduateKnowledgeOperation.COUNT,
                new GraduateKnowledgeFilters(List.of(university()), List.of(), List.of(), null, null, null));

        assertTrue(adapter.retrieveContext(query).formattedContext().contains("Count: 1"));
    }

    @Test
    void departmentExistsUsesExactNormalizedName() {
        GraduateKnowledgeQuery query = query(GraduateKnowledgeResource.DEPARTMENT, GraduateKnowledgeOperation.EXISTS,
                new GraduateKnowledgeFilters(List.of(university()), List.of(), List.of(), null, null, " computer science "));

        assertTrue(adapter.retrieveContext(query).formattedContext().contains("Exists: true"));
    }

    @Test
    void programCountUsesStructuredForeignKeysAndDistinctIds() {
        GraduateKnowledgeQuery query = query(GraduateKnowledgeResource.PROGRAM, GraduateKnowledgeOperation.COUNT,
                new GraduateKnowledgeFilters(List.of(university()), List.of("MASTER"), List.of(), null, null, "Computer Science"));

        assertTrue(adapter.retrieveContext(query).formattedContext().contains("Count: 1"));
    }

    private ResolvedUniversity university() {
        return new ResolvedUniversity(universityId, "Academic Structure Test University", "ASTU");
    }

    private GraduateKnowledgeQuery query(GraduateKnowledgeResource resource, GraduateKnowledgeOperation operation,
                                         GraduateKnowledgeFilters filters) {
        return new GraduateKnowledgeQuery(GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP,
                resource, operation, filters, null, false, false);
    }
}
