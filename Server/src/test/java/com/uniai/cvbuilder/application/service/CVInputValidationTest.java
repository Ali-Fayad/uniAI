package com.uniai.cvbuilder.application.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CVInputValidationTest {

    @Test
    void rejectsReversedDateRanges() {
        assertThrows(IllegalArgumentException.class, () -> CVInputValidation.validateDateRange(
                LocalDate.of(2025, 2, 1), LocalDate.of(2024, 2, 1), "Education"));
    }

    @Test
    void rejectsEndDateForCurrentExperience() {
        assertThrows(IllegalArgumentException.class, () -> CVInputValidation.validateCurrentExperience(
                LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1), true));
    }

    @Test
    void acceptsOpenCurrentExperience() {
        assertDoesNotThrow(() -> CVInputValidation.validateCurrentExperience(
                LocalDate.of(2024, 1, 1), null, true));
    }
}
