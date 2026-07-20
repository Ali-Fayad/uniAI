package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.citation.GraduateCitation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateRouteFinalContextBuilderTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GraduateRouteFinalContextBuilder builder = new GraduateRouteFinalContextBuilder();

    @Test
    void includesRouteArgumentsDataWarningsEmptyStateAndSources() {
        GraduateRouteExecutionResult result = new GraduateRouteExecutionResult(
                GraduateAiRoute.GET_PROGRAM_TUITION,
                objectMapper.createObjectNode().put("university", "American University of Beirut")
                        .put("programName", "Computer Science"),
                "Amount: 1136.00 USD per credit",
                List.of(new GraduateCitation("tuition-1", "S1", "AUB Tuition 2026-2027",
                        "https://example.edu/tuition", "TUITION", 1L,
                        "American University of Beirut", 6L, "Master of Science in Computer Science")),
                List.of("Result limit was applied."),
                false,
                null);

        String context = builder.build(result);

        assertTrue(context.contains("Selected route: GET_PROGRAM_TUITION"));
        assertTrue(context.contains("American University of Beirut"));
        assertTrue(context.contains("Amount: 1136.00 USD per credit"));
        assertTrue(context.contains("Result limit was applied."));
        assertTrue(context.contains("[S1] AUB Tuition 2026-2027"));
    }

    @Test
    void directAiContextExplicitlySkipsGraduateRetrieval() {
        String context = builder.build(GraduateRouteExecutionResult.direct(
                objectMapper.createObjectNode().put("reason", "GREETING"), GraduateDirectAiReason.GREETING));

        assertTrue(context.contains("Graduate database retrieval: skipped"));
        assertFalse(context.contains("Retrieved database context"));
    }
}
