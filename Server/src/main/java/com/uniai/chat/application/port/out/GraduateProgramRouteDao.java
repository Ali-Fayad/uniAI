package com.uniai.chat.application.port.out;

import java.util.List;

/** Typed, read-only DAO for program routes. */
public interface GraduateProgramRouteDao {
    ProgramPage findPrograms(ProgramCriteria criteria);

    long countPrograms(ProgramCriteria criteria);

    List<GroupCountRow> countProgramsBy(ProgramCriteria criteria, ProgramGrouping grouping);

    List<ProgramEvidenceRow> findTracks(ProgramCriteria criteria);

    List<ProgramEvidenceRow> findLanguages(ProgramCriteria criteria);

    List<ProgramEvidenceRow> findSources(ProgramCriteria criteria);

    enum ProgramGrouping { UNIVERSITY, FACULTY, DEPARTMENT }

    record ProgramCriteria(
            List<Long> universityIds,
            String searchQuery,
            String programName,
            String degreeLevel,
            String facultyName,
            String departmentName,
            String language,
            String city,
            int limit
    ) {
        public ProgramCriteria {
            universityIds = universityIds == null ? List.of() : List.copyOf(universityIds);
            if (limit < 1 || limit > 200) throw new IllegalArgumentException("Program limit must be between 1 and 200");
        }
    }

    record ProgramPage(List<ProgramRow> rows, long totalMatches) {
        public ProgramPage {
            rows = rows == null ? List.of() : List.copyOf(rows);
        }

        public boolean truncated() {
            return totalMatches > rows.size();
        }
    }

    record ProgramRow(
            long programId,
            long universityId,
            String universityName,
            String universityAcronym,
            String programKey,
            String major,
            String officialDegreeName,
            String degreeType,
            String facultyName,
            String departmentName,
            Integer credits,
            String duration,
            String language,
            String deliveryMode,
            String thesisOption,
            String description,
            String officialUrl,
            String sourceTitle,
            String sourceUrl
    ) {}

    record GroupCountRow(Long groupId, String groupName, String universityName, long count) {}

    record ProgramEvidenceRow(
            long programId,
            long universityId,
            String universityName,
            String programName,
            String kind,
            String value,
            String details,
            String sourceTitle,
            String sourceUrl
    ) {}
}
