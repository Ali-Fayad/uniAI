package com.uniai.chat.application.interpretation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CanonicalGraduateQueryDraftValidatorTest {
    private final CanonicalGraduateQueryDraftValidator validator = new CanonicalGraduateQueryDraftValidator();

    @Test
    void acceptsProgramTuitionAggregation() {
        CanonicalGraduateQueryDraft draft = new CanonicalGraduateQueryDraft(
                2, "PROGRAM", "AGGREGATE",
                new CanonicalGraduateQueryDraft.Filters(
                        java.util.List.of("University of Balamand"), java.util.List.of("MASTER"),
                        null, null, null, null, java.util.List.of(), java.util.List.of(), java.util.List.of(),
                        new CanonicalGraduateQueryDraft.Tuition(null, null, "USD", "PER_YEAR", null, "PROGRAM")
                ),
                new CanonicalGraduateQueryDraft.Aggregation("AVG", "TUITION"), null, null,
                "LIST", null, false, java.util.List.of());

        assertDoesNotThrow(() -> validator.validate(draft));
    }

    @Test
    void rejectsInvalidSchemaAndEnums() {
        CanonicalGraduateQueryDraft invalid = new CanonicalGraduateQueryDraft(
                1, "PROGRAM", "LIST", CanonicalGraduateQueryDraft.Filters.empty(),
                null, null, null, "LIST", null, false, java.util.List.of());

        assertThrows(IllegalArgumentException.class, () -> validator.validate(invalid));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(new CanonicalGraduateQueryDraft(
                2, "PROGRAM", "AGGREGATE", CanonicalGraduateQueryDraft.Filters.empty(),
                new CanonicalGraduateQueryDraft.Aggregation("MEDIAN", "TUITION"), null, null,
                "LIST", null, false, java.util.List.of())));
    }

    @Test
    void rejectsMalformedComparisonAndOversizedInput() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(new CanonicalGraduateQueryDraft(
                2, "PROGRAM", "LIST", CanonicalGraduateQueryDraft.Filters.empty(), null, null,
                new CanonicalGraduateQueryDraft.Comparison("PROGRAM_COUNT"), "LIST", null, false, java.util.List.of())));

        assertThrows(IllegalArgumentException.class, () -> validator.validate(new CanonicalGraduateQueryDraft(
                2, "PROGRAM", "LIST",
                new CanonicalGraduateQueryDraft.Filters(
                        java.util.List.of("A", "B", "C", "D"), java.util.List.of(), null, null, null, null,
                        java.util.List.of(), java.util.List.of(), java.util.List.of(), null),
                null, null, null, "LIST", null, false, java.util.List.of())));
    }
}
