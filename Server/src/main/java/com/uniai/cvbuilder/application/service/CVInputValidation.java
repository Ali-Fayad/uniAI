package com.uniai.cvbuilder.application.service;

import java.time.LocalDate;

/** Central business validation for cross-field CV dates. */
final class CVInputValidation {

    private CVInputValidation() {
    }

    static void validateDateRange(LocalDate start, LocalDate end, String field) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException(field + " end date cannot be before its start date");
        }
    }

    static void validateCurrentExperience(LocalDate start, LocalDate end, Boolean current) {
        validateDateRange(start, end, "Experience");
        if (Boolean.TRUE.equals(current) && end != null) {
            throw new IllegalArgumentException("Current experience cannot have an end date");
        }
    }
}
