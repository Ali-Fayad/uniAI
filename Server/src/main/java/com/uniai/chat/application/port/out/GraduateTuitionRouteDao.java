package com.uniai.chat.application.port.out;

import java.math.BigDecimal;
import java.util.List;

/** Typed, read-only DAO for tuition and fee routes. */
public interface GraduateTuitionRouteDao {
    TuitionPage findTuition(TuitionCriteria criteria);

    List<TuitionAggregateRow> aggregateTuition(TuitionCriteria criteria);

    List<UniversityTuitionRankingRow> rankUniversitiesByTuition(TuitionRankingCriteria criteria);

    List<ProgramTuitionRankingRow> rankProgramsByTuition(TuitionRankingCriteria criteria);

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

    record TuitionRankingCriteria(
            List<Long> universityIds,
            List<String> programs,
            List<String> faculties,
            List<String> departments,
            List<String> degreeTypes,
            List<String> cities,
            String academicYear,
            String currency,
            String billingBasis,
            String order,
            int limit
    ) {
        public TuitionRankingCriteria {
            universityIds = universityIds == null ? List.of() : List.copyOf(universityIds);
            programs = programs == null ? List.of() : List.copyOf(programs);
            faculties = faculties == null ? List.of() : List.copyOf(faculties);
            departments = departments == null ? List.of() : List.copyOf(departments);
            degreeTypes = degreeTypes == null ? List.of() : List.copyOf(degreeTypes);
            cities = cities == null ? List.of() : List.copyOf(cities);
            if (limit < 1 || limit > 100) throw new IllegalArgumentException("Ranking limit must be between 1 and 100");
        }
    }

    record UniversityTuitionRankingRow(
            long universityId, String universityName, String universityAcronym,
            String academicYear, String currency, String billingBasis, String scopeLevel,
            BigDecimal averageAmount, BigDecimal minimumAmount, BigDecimal maximumAmount,
            long matchingRecordCount
    ) {}

    record ProgramTuitionRankingRow(
            long programId, String programName, long universityId, String universityName,
            String universityAcronym, String academicYear, String currency, String billingBasis, String scopeLevel,
            BigDecimal averageAmount, BigDecimal minimumAmount, BigDecimal maximumAmount,
            long matchingRecordCount
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
