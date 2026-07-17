package com.uniai.chat.infrastructure.retrieval;

import com.uniai.chat.application.citation.GraduateKnowledgeRetrievalResult;
import com.uniai.chat.application.retrieval.GraduateKnowledgeAggregation;
import com.uniai.chat.application.retrieval.GraduateKnowledgeAggregationFunction;
import com.uniai.chat.application.retrieval.GraduateKnowledgeFilters;
import com.uniai.chat.application.retrieval.GraduateKnowledgeFollowUpContext;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeOperation;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeResource;
import com.uniai.chat.application.retrieval.GraduateKnowledgeSort;
import com.uniai.chat.application.retrieval.GraduateKnowledgeThresholdOperator;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import com.uniai.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "ai.provider=placeholder")
class SqlGraduateTuitionAnalyticsIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private SqlGraduateKnowledgeRetrievalAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Fixture fixture;

    @BeforeEach
    void setUp() {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT gtr.university_id, u.name AS university_name, u.acronym AS university_acronym,
                       dt.code AS degree_type_code, gtr.currency, gtr.billing_basis, gtr.academic_year
                FROM graduate_tuition_rate gtr
                JOIN university u ON u.id = gtr.university_id
                LEFT JOIN graduate_program gp ON gp.id = gtr.program_id
                LEFT JOIN degree_type dt ON dt.id = gp.degree_type_id
                WHERE gtr.amount IS NOT NULL
                ORDER BY gtr.university_id, gtr.id
                LIMIT 1
                """);
        fixture = new Fixture(
                longValue(row.get("university_id")),
                stringValue(row.get("university_name")),
                stringValue(row.get("university_acronym")),
                stringValue(row.get("degree_type_code")),
                stringValue(row.get("currency")),
                stringValue(row.get("billing_basis")),
                stringValue(row.get("academic_year"))
        );
    }

    @Test
    void sqlAggregationSupportsMinMaxAndRangeWithinStoredCompatibilityGroups() {
        GraduateKnowledgeRetrievalResult minimum = retrieve(GraduateKnowledgeAggregationFunction.MIN, null, null);
        GraduateKnowledgeRetrievalResult maximum = retrieve(GraduateKnowledgeAggregationFunction.MAX, null, null);
        GraduateKnowledgeRetrievalResult range = retrieve(GraduateKnowledgeAggregationFunction.RANGE, null, null);

        assertTrue(minimum.formattedContext().contains("Computed minimum:"), minimum.formattedContext());
        assertTrue(maximum.formattedContext().contains("Computed maximum:"), maximum.formattedContext());
        assertTrue(range.formattedContext().contains("Computed range:"), range.formattedContext());
        assertFalse(range.citations().isEmpty());
    }

    @Test
    void sqlAggregationAppliesTypedThresholdAndBoundedLimit() {
        GraduateKnowledgeRetrievalResult result = retrieve(
                GraduateKnowledgeAggregationFunction.AVG,
                GraduateKnowledgeThresholdOperator.GTE,
                new BigDecimal("0")
        );

        assertTrue(result.formattedContext().contains("Tuition aggregation:"), result.formattedContext());
        assertFalse(result.citations().isEmpty());
    }

    private GraduateKnowledgeRetrievalResult retrieve(
            GraduateKnowledgeAggregationFunction function,
            GraduateKnowledgeThresholdOperator thresholdOperator,
            BigDecimal thresholdValue
    ) {
        GraduateKnowledgeFilters filters = new GraduateKnowledgeFilters(
                List.of(new ResolvedUniversity(fixture.universityId(), fixture.universityName(), fixture.universityAcronym())),
                fixture.degreeType() == null ? List.of() : List.of(fixture.degreeType()),
                List.of(), null, null, null, List.of(), List.of(), null,
                fixture.currency(), fixture.billingBasis(), fixture.academicYear(), null,
                thresholdOperator == null ? GraduateKnowledgeThresholdOperator.NONE : thresholdOperator,
                thresholdValue
        );
        GraduateKnowledgeQuery query = new GraduateKnowledgeQuery(
                GraduateKnowledgeIntent.TUITION_AGGREGATION,
                GraduateKnowledgeResource.PROGRAM,
                GraduateKnowledgeOperation.AGGREGATE,
                filters,
                new GraduateKnowledgeAggregation(function, "tuition"),
                GraduateKnowledgeSort.empty(),
                5,
                GraduateKnowledgeFollowUpContext.empty(),
                null,
                false,
                false
        );
        return adapter.retrieveContext(query);
    }

    private Long longValue(Object value) {
        return value instanceof Number number ? number.longValue() : Long.valueOf(value.toString());
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private record Fixture(Long universityId, String universityName, String universityAcronym,
                           String degreeType, String currency, String billingBasis, String academicYear) {
    }
}
