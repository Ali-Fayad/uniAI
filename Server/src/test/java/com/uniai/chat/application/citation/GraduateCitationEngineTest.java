package com.uniai.chat.application.citation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateCitationEngineTest {

    @Test
    void extractCitationsShouldDeduplicateAndIgnoreUnknownLabels() {
        List<GraduateCitation> available = List.of(
                new GraduateCitation("program-1", "S1", "AUB CS", "https://aub.edu.lb/cs", "PROGRAM", 1L, "AUB", 11L, "Computer Science"),
                new GraduateCitation("program-2", "S2", "LAU MBA", "https://lau.edu.lb/mba", "PROGRAM", 2L, "LAU", 22L, "MBA")
        );

        List<GraduateCitation> extracted = GraduateCitationEngine.extractCitations(
                "Answer [S1], [S1], [S99], [bad], and https://example.com",
                available
        );

        assertEquals(1, extracted.size());
        assertEquals("S1", extracted.get(0).label());
    }

    @Test
    void extractCitationsShouldReturnEmptyListWhenNoLabelsArePresent() {
        List<GraduateCitation> extracted = GraduateCitationEngine.extractCitations(
                "This response has no citation labels.",
                List.of(new GraduateCitation("program-1", "S1", "AUB CS", "https://aub.edu.lb/cs", "PROGRAM", 1L, "AUB", 11L, "Computer Science"))
        );

        assertEquals(0, extracted.size());
    }

    @Test
    void buildRegistryBlockShouldUseStableCitationLabels() {
        String registry = GraduateCitationEngine.buildRegistryBlock(List.of(
                new GraduateCitation("program-1", "S1", "AUB CS", "https://aub.edu.lb/cs", "PROGRAM", 1L, "AUB", 11L, "Computer Science"),
                new GraduateCitation("program-2", "S2", "LAU MBA", "https://lau.edu.lb/mba", "PROGRAM", 2L, "LAU", 22L, "MBA")
        ));

        assertTrue(registry.contains("Sources:"));
        assertTrue(registry.contains("[S1] AUB CS"));
        assertTrue(registry.contains("[S2] LAU MBA"));
    }

    @Test
    void filterCitationsPresentInContextShouldKeepOnlySurvivingCitationsAndPreserveOrder() {
        List<GraduateCitation> citations = List.of(
                new GraduateCitation("program-1", "S1", "AUB CS", "https://aub.edu.lb/cs", "PROGRAM", 1L, "AUB", 11L, "Computer Science"),
                new GraduateCitation("program-2", "S2", "LAU MBA", "https://lau.edu.lb/mba", "PROGRAM", 2L, "LAU", 22L, "MBA"),
                new GraduateCitation("program-3", "S3", "USJ Data Science", "https://usj.edu.lb/ds", "PROGRAM", 3L, "USJ", 33L, "Data Science")
        );

        List<GraduateCitation> filtered = GraduateCitationEngine.filterCitationsPresentInContext(
                citations,
                List.of(
                        "Retrieved official context: [S1] [S2]",
                        "AUB CS program details remain in scope.",
                        "LAU MBA program details remain in scope.",
                        "USJ Data Science program details were trimmed out."
                )
        );

        assertNotSame(citations, filtered);
        assertEquals(2, filtered.size());
        assertEquals("S1", filtered.get(0).label());
        assertEquals("S2", filtered.get(1).label());
        assertEquals(3, citations.size());
    }

    @Test
    void filterCitationsPresentInContextShouldDistinguishS1FromS10AndHandleBlankContext() {
        List<GraduateCitation> citations = List.of(
                new GraduateCitation("program-1", "S1", "AUB CS", "https://aub.edu.lb/cs", "PROGRAM", 1L, "AUB", 11L, "Computer Science"),
                new GraduateCitation("program-10", "S10", "AUB AI", "https://aub.edu.lb/ai", "PROGRAM", 1L, "AUB", 12L, "Artificial Intelligence")
        );

        List<GraduateCitation> exactLabelMatch = GraduateCitationEngine.filterCitationsPresentInContext(
                citations,
                List.of("Final prompt labels: [S10] only")
        );

        assertEquals(1, exactLabelMatch.size());
        assertEquals("S10", exactLabelMatch.get(0).label());

        List<GraduateCitation> empty = GraduateCitationEngine.filterCitationsPresentInContext(citations, List.of());
        assertEquals(0, empty.size());
        assertEquals(0, GraduateCitationEngine.filterCitationsPresentInContext(citations, List.of("   ")).size());
        assertEquals(0, GraduateCitationEngine.filterCitationsPresentInContext(citations, null).size());
    }

    @Test
    void filterCitationsPresentInContextShouldSupportMultipleEntries() {
        List<GraduateCitation> citations = List.of(
                new GraduateCitation("program-1", "S1", "AUB CS", "https://aub.edu.lb/cs", "PROGRAM", 1L, "AUB", 11L, "Computer Science"),
                new GraduateCitation("program-2", "S2", "LAU MBA", "https://lau.edu.lb/mba", "PROGRAM", 2L, "LAU", 22L, "MBA"),
                new GraduateCitation("program-3", "S3", "USJ Data Science", "https://usj.edu.lb/ds", "PROGRAM", 3L, "USJ", 33L, "Data Science")
        );

        List<GraduateCitation> filtered = GraduateCitationEngine.filterCitationsPresentInContext(
                citations,
                List.of(
                        "Partial context mentions AUB CS program details.",
                        "Later context mentions USJ Data Science program details."
                )
        );

        assertEquals(List.of("S1", "S3"), filtered.stream().map(GraduateCitation::label).toList());
    }
}
