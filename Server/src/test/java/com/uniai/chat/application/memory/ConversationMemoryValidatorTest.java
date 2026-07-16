package com.uniai.chat.application.memory;

import com.uniai.catalog.domain.model.UniversityCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversationMemoryValidatorTest {

    private final ConversationMemoryValidator validator = new ConversationMemoryValidator();

    @Test
    void validatePatchShouldAcceptTrustedUniversityAndPreferencePatch() {
        ConversationMemoryPatch patch = new ConversationMemoryPatch(
                ConversationMemory.SCHEMA_VERSION,
                "PROGRAM_LOOKUP",
                true,
                List.of("AUB"),
                List.of(),
                List.of(),
                List.of("MASTER"),
                List.of(),
                List.of(),
                List.of("AUB"),
                List.of("tuition"),
                List.of(),
                List.of("prefer online"),
                List.of(),
                new ConversationPreferences("English", "lower tuition first", "online"),
                List.of("pendingTopics")
        );

        ConversationMemoryValidator.ValidationResult result = validator.validatePatch(
                patch,
                List.of(UniversityCatalog.builder().id(1L).name("American University of Beirut").acronym("AUB").build())
        );

        assertTrue(result.isValid());
        assertFalse(result.unsupported());
    }

    @Test
    void validatePatchShouldAcceptGraduateOverviewIntent() {
        ConversationMemoryPatch patch = new ConversationMemoryPatch(
                ConversationMemory.SCHEMA_VERSION,
                "GRADUATE_OVERVIEW",
                false,
                List.of("AUB"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of()
        );

        ConversationMemoryValidator.ValidationResult result = validator.validatePatch(
                patch,
                List.of(UniversityCatalog.builder().id(1L).name("American University of Beirut").acronym("AUB").build())
        );

        assertTrue(result.isValid());
    }

    @Test
    void validatePatchShouldRejectInventedUniversityAndUnsupportedDegree() {
        ConversationMemoryPatch inventedUniversity = new ConversationMemoryPatch(
                ConversationMemory.SCHEMA_VERSION,
                "PROGRAM_LOOKUP",
                null,
                List.of("Invented University"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of()
        );

        ConversationMemoryValidator.ValidationResult universityResult = validator.validatePatch(
                inventedUniversity,
                List.of(UniversityCatalog.builder().id(1L).name("American University of Beirut").acronym("AUB").build())
        );

        ConversationMemoryPatch unsupportedDegree = new ConversationMemoryPatch(
                ConversationMemory.SCHEMA_VERSION,
                "PROGRAM_LOOKUP",
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of("BACHELOR"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of()
        );

        ConversationMemoryValidator.ValidationResult degreeResult = validator.validatePatch(unsupportedDegree, List.of());

        assertFalse(universityResult.isValid());
        assertFalse(degreeResult.isValid());
        assertTrue(degreeResult.unsupported());
    }

    @Test
    void validatePatchShouldRejectInvalidIntentAndUnknownClearField() {
        ConversationMemoryPatch invalidIntent = new ConversationMemoryPatch(
                ConversationMemory.SCHEMA_VERSION,
                "HISTORY_LOOKUP",
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of("unknownField")
        );

        ConversationMemoryValidator.ValidationResult result = validator.validatePatch(
                invalidIntent,
                List.of(UniversityCatalog.builder().id(1L).name("American University of Beirut").acronym("AUB").build())
        );

        assertFalse(result.isValid());
    }

    @Test
    void validatePatchShouldRejectUnknownClearField() {
        ConversationMemoryPatch patch = new ConversationMemoryPatch(
                ConversationMemory.SCHEMA_VERSION,
                "PROGRAM_LOOKUP",
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of("unknownField")
        );

        ConversationMemoryValidator.ValidationResult result = validator.validatePatch(
                patch,
                List.of(UniversityCatalog.builder().id(1L).name("American University of Beirut").acronym("AUB").build())
        );

        assertFalse(result.isValid());
    }
}
