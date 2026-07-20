package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraduateRoutePlanParserTest {
    private GraduateRoutePlanParser parser;

    @BeforeEach
    void setUp() {
        parser = new GraduateRoutePlanParser(new GraduateAiRouteCatalog(), new ObjectMapper());
    }

    @Test
    void parsesAValidRouteIntoItsDedicatedArgumentType() {
        ValidatedGraduateRoutePlan<?> plan = parser.parse("""
                {"route":"GET_PROGRAM_TUITION","arguments":{
                  "university":"AUB","programName":"Computer Science",
                  "degreeType":"MASTER","academicYear":null,"limit":20
                }}
                """);

        assertEquals(GraduateAiRoute.GET_PROGRAM_TUITION, plan.route());
        GraduateRouteArguments.ProgramTuitionArguments arguments = assertInstanceOf(
                GraduateRouteArguments.ProgramTuitionArguments.class, plan.arguments());
        assertEquals("AUB", arguments.university());
        assertEquals("Computer Science", arguments.programName());
        assertEquals(GraduateRouteArguments.DegreeLevel.MASTER, arguments.degreeType());
        assertEquals(20, arguments.limit());
    }

    @Test
    void rejectsMalformedAndUnknownRoutes() {
        assertInvalid("not-json");
        assertInvalid("{\"route\":\"DELETE_ALL_DATA\",\"arguments\":{}}");
        assertInvalid("{\"arguments\":{}}");
        assertInvalid("{\"route\":\"LIST_PROGRAMS\"}");
    }

    @Test
    void rejectsExtraTopLevelAndRouteArgumentFields() {
        assertInvalid("{\"route\":\"LIST_PROGRAMS\",\"arguments\":{},\"sql\":\"select 1\"}");
        assertInvalid("{\"route\":\"LIST_PROGRAMS\",\"arguments\":{\"methodName\":\"deleteAll\"}}");
    }

    @Test
    void rejectsMissingRequiredArgumentsInvalidEnumsAndWrongTypes() {
        assertInvalid("{\"route\":\"SEARCH_PROGRAMS\",\"arguments\":{}}");
        assertInvalid("{\"route\":\"DIRECT_AI_RESPONSE\",\"arguments\":{\"reason\":\"DELETE_REQUEST\"}}");
        assertInvalid("{\"route\":\"GET_PROGRAM_TUITION\",\"arguments\":{\"programName\":42}}");
    }

    @Test
    void enforcesTheRouteSpecificMaximumLimit() {
        parser.parse("{\"route\":\"LIST_PROGRAMS\",\"arguments\":{\"limit\":200}}");
        assertInvalid("{\"route\":\"LIST_PROGRAMS\",\"arguments\":{\"limit\":201}}");
        assertInvalid("{\"route\":\"LIST_PROGRAMS\",\"arguments\":{\"limit\":0}}");
        assertInvalid("{\"route\":\"LIST_PROGRAMS\",\"arguments\":{\"limit\":2.5}}");
    }

    private void assertInvalid(String json) {
        assertThrows(GraduateRoutePlanningException.class, () -> parser.parse(json));
    }
}
