package com.uniai.chat.application.port.out;

import java.math.BigDecimal;
import java.util.List;

/** Typed, read-only DAO for tuition and fee routes. */
public interface GraduateTuitionRouteDao {
    TuitionPage findTuition(TuitionCriteria criteria);

    List<TuitionAggregateRow> aggregateTuition(TuitionCriteria criteria);

    FeePage findFees(TuitionCriteria criteria);

    record TuitionCriteria(
            List<Long> universityIds,
            String programName,
            String degreeLevel,
            String facultyName,
            String departmentName,
            String academicYear,
            String currency,
            String billingBasis,
            String scopeLevel,
            int limit
    ) {
        public TuitionCriteria {
            universityIds = universityIds == null ? List.of() : List.copyOf(universityIds);
            if (limit < 1 || limit > 100) throw new IllegalArgumentException("Tuition limit must be between 1 and 100");
        }
    }

    record TuitionPage(List<TuitionRow> rows, long totalMatches) {
        public TuitionPage { rows = rows == null ? List.of() : List.copyOf(rows); }
        public boolean truncated() { return totalMatches > rows.size(); }
    }

    record TuitionRow(
            long tuitionId,
            long universityId,
            String universityName,
            String universityAcronym,
            Long programId,
            String programName,
            String degreeType,
            String facultyName,
            String departmentName,
            String scopeLevel,
            String academicYear,
            String currency,
            String billingBasis,
            BigDecimal amount,
            String category,
            String notes,
            String sourceTitle,
            String sourceUrl
    ) {}

    record TuitionAggregateRow(
            long universityId,
            String universityName,
            String universityAcronym,
            String academicYear,
            String currency,
            String billingBasis,
            String scopeLevel,
            long recordCount,
            BigDecimal averageAmount,
            BigDecimal minimumAmount,
            BigDecimal maximumAmount
    ) {}

    record FeePage(List<FeeRow> rows, long totalMatches) {
        public FeePage { rows = rows == null ? List.of() : List.copyOf(rows); }
        public boolean truncated() { return totalMatches > rows.size(); }
    }

    record FeeRow(
            long feeId,
            long universityId,
            String universityName,
            String programName,
            String facultyName,
            String departmentName,
            String scopeLevel,
            String academicYear,
            String feeName,
            String billingBasis,
            String currency,
            BigDecimal amount,
            String category,
            String notes,
            String sourceTitle,
            String sourceUrl
    ) {}
}
