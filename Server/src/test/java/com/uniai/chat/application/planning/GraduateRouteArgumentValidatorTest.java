package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraduateRouteArgumentValidatorTest {
    private GraduateRoutePlanParser parser;
    private GraduateRouteArgumentValidator validator;

    @BeforeEach
    void setUp() {
        parser = new GraduateRoutePlanParser(new GraduateAiRouteCatalog(), new ObjectMapper());
        validator = new GraduateRouteArgumentValidator();
    }

    @Test
    void validatesAcademicYearCurrencyAndComparisonCardinality() {
        assertInvalid("{\"route\":\"GET_TUITION\",\"arguments\":{\"academicYear\":\"next year\"}}");
        assertInvalid("{\"route\":\"GET_TUITION\",\"arguments\":{\"currency\":\"dollars\"}}");
        assertInvalid("{\"route\":\"COMPARE_TUITION\",\"arguments\":{\"universities\":[\"AUB\"]}}");
        assertDoesNotThrow(() -> validator.validate(parser.parse(
                "{\"route\":\"COMPARE_TUITION\",\"arguments\":{\"universities\":[\"AUB\",\"LAU\"],\"academicYear\":\"2026-2027\",\"currency\":\"USD\"}}")));
    }

    @Test
    void rejectsAnUnscopedCampusExistenceCheck() {
        assertInvalid("{\"route\":\"CHECK_CAMPUS_EXISTS\",\"arguments\":{}}");
    }

    @Test
    void acceptsUnscopedTuitionRankingWithOptionalCandidateLists() {
        assertDoesNotThrow(() -> validator.validate(parser.parse(
                "{\"route\":\"RANK_UNIVERSITIES_BY_TUITION\",\"arguments\":{"
                        + "\"universities\":[],\"programs\":[\"Computer Science\"],"
                        + "\"degreeTypes\":[\"MASTER\"],\"cities\":[],"
                        + "\"currency\":\"USD\",\"tuitionUnit\":\"PER_CREDIT\","
                        + "\"order\":\"ASC\",\"limit\":5}}")));
    }

    @Test
    void rejectsAnUnboundedTuitionRanking() {
        assertThrows(GraduateRoutePlanningException.class, () -> parser.parse(
                "{\"route\":\"RANK_PROGRAMS_BY_TUITION\",\"arguments\":{\"limit\":11}}"));
    }

    private void assertInvalid(String json) {
        ValidatedGraduateRoutePlan<?> plan = parser.parse(json);
        assertThrows(GraduateRoutePlanningException.class, () -> validator.validate(plan));
    }
}
