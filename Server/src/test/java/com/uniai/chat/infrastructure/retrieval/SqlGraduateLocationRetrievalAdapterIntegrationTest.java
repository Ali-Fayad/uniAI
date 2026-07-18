package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.retrieval.GraduateKnowledgeFilters;
import com.uniai.chat.application.retrieval.GraduateKnowledgeFollowUpContext;
import com.uniai.chat.application.retrieval.GraduateKnowledgeComparisonDimension;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "ai.provider=placeholder")
class SqlGraduateLocationRetrievalAdapterIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private SqlGraduateLocationRetrievalAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedStructuredLocationRows() {
        jdbcTemplate.update("INSERT INTO university (name, acronym, city, campus_name, campus_type) VALUES (?, ?, ?, ?, ?)",
                "Location Test University", "LTX", "Beirut", null, null);
        jdbcTemplate.update("INSERT INTO university (name, acronym, city, campus_name, campus_type) VALUES (?, ?, ?, ?, ?)",
                "Location Test University", "LTX", "Beirut", "Main Campus", "Main");
        jdbcTemplate.update("INSERT INTO university (name, acronym, city, campus_name, campus_type) VALUES (?, ?, ?, ?, ?)",
                "Location Test University", "LTX", "Tripoli", "North Campus", "Branch");
        jdbcTemplate.update("INSERT INTO university (name, acronym, city, campus_name, campus_type) VALUES (?, ?, ?, ?, ?)",
                "Location Test Other University", "LTO", "Beirut", null, null);
    }

    @AfterEach
    void removeStructuredLocationRows() {
        jdbcTemplate.update("DELETE FROM university WHERE name LIKE 'Location Test %'");
    }

    @Test
    void campusCountUsesDistinctStructuredCampusRows() {
        GraduateKnowledgeQuery query = query(GraduateKnowledgeResource.CAMPUS, GraduateKnowledgeOperation.COUNT,
                new GraduateKnowledgeFilters(List.of(), List.of(), List.of(), "Beirut"));

        assertTrue(adapter.retrieveContext(query).formattedContext().contains("Structured campus count: 1"));
    }

    @Test
    void universityCountUsesLogicalInstitutionsByAcronym() {
        GraduateKnowledgeQuery query = query(GraduateKnowledgeResource.UNIVERSITY, GraduateKnowledgeOperation.COUNT,
                new GraduateKnowledgeFilters(List.of(), List.of(), List.of(), "Beirut"));

        assertTrue(adapter.retrieveContext(query).formattedContext().contains("Structured university count: 2"));
    }

    @Test
    void distinguishesUnavailableLocationDataFromNoMatchingRows() {
        GraduateKnowledgeQuery noMatchingCity = query(GraduateKnowledgeResource.CAMPUS, GraduateKnowledgeOperation.LIST,
                new GraduateKnowledgeFilters(List.of(), List.of(), List.of(), "Sidon"));
        assertTrue(adapter.retrieveContext(noMatchingCity).formattedContext().contains("No matching rows"));

        Map<String, Object> institution = jdbcTemplate.queryForMap(
                "SELECT id, name, acronym FROM university WHERE name = ? AND campus_name IS NULL",
                "Location Test Other University");
        GraduateKnowledgeQuery unavailableCampus = query(GraduateKnowledgeResource.CAMPUS, GraduateKnowledgeOperation.LIST,
                new GraduateKnowledgeFilters(List.of(new ResolvedUniversity(
                        ((Number) institution.get("id")).longValue(),
                        (String) institution.get("name"),
                        (String) institution.get("acronym")
                )), List.of(), List.of(), null));
        assertTrue(adapter.retrieveContext(unavailableCampus).formattedContext().contains("Location data is unavailable"));
    }

    @Test
    void campusComparisonUsesBoundedGroupedCounts() {
        Map<String, Object> first = jdbcTemplate.queryForMap(
                "SELECT id, name, acronym FROM university WHERE acronym = ? AND campus_name IS NOT NULL ORDER BY id LIMIT 1", "LTX");
        Map<String, Object> second = jdbcTemplate.queryForMap(
                "SELECT id, name, acronym FROM university WHERE acronym = ? AND city = ? AND campus_name IS NOT NULL", "LTX", "Tripoli");
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.LOCATION_LOOKUP,
                GraduateKnowledgeResource.CAMPUS,
                GraduateKnowledgeOperation.COMPARE,
                new GraduateKnowledgeFilters(List.of(
                        new ResolvedUniversity(((Number) first.get("id")).longValue(), (String) first.get("name"), (String) first.get("acronym")),
                        new ResolvedUniversity(((Number) second.get("id")).longValue(), (String) second.get("name"), (String) second.get("acronym"))
                ), List.of(), List.of()),
                com.uniai.chat.application.retrieval.GraduateKnowledgeAggregation.empty(),
                com.uniai.chat.application.retrieval.GraduateKnowledgeSort.empty(), 5,
                new GraduateKnowledgeFollowUpContext(null, null, GraduateKnowledgeResource.CAMPUS,
                        GraduateKnowledgeOperation.COMPARE, List.of(), GraduateKnowledgeComparisonDimension.CAMPUS_COUNT),
                null, true, false
        );

        assertTrue(adapter.retrieveContext(query).formattedContext().contains("Campus comparison:"));
    }

    private GraduateKnowledgeQuery query(GraduateKnowledgeResource resource, GraduateKnowledgeOperation operation,
                                         GraduateKnowledgeFilters filters) {
        return new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.LOCATION_LOOKUP,
                resource,
                operation,
                filters,
                null,
                false,
                false
        );
    }
}
