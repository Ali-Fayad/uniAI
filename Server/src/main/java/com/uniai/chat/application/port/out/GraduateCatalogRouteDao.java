package com.uniai.chat.application.port.out;

import java.math.BigDecimal;
import java.util.List;

/** Typed, read-only DAO for university geography and academic structure routes. */
public interface GraduateCatalogRouteDao {
    List<UniversityRow> findUniversities(CatalogCriteria criteria);
    long countUniversities(CatalogCriteria criteria);
    List<CampusRow> findCampuses(CatalogCriteria criteria);
    long countCampuses(CatalogCriteria criteria);
    List<AcademicRow> findFaculties(CatalogCriteria criteria);
    long countFaculties(CatalogCriteria criteria);
    List<AcademicRow> findDepartments(CatalogCriteria criteria);
    long countDepartments(CatalogCriteria criteria);
    List<UniversityStatisticsRow> universityStatistics(CatalogCriteria criteria);

    record CatalogCriteria(
            List<Long> universityIds,
            String searchQuery,
            String city,
            String campusName,
            String facultyName,
            String departmentName,
            String country,
            int limit
    ) {
        public CatalogCriteria {
            universityIds = universityIds == null ? List.of() : List.copyOf(universityIds);
            if (limit < 1 || limit > 200) throw new IllegalArgumentException("Catalog limit must be between 1 and 200");
        }
    }

    record UniversityRow(long id, String name, String acronym, String nameAr, String country) {}
    record CampusRow(long id, long universityId, String universityName, String universityAcronym,
                     String name, String campusType, String city, String locality,
                     BigDecimal latitude, BigDecimal longitude) {}
    record AcademicRow(long id, long universityId, String universityName, String universityAcronym,
                       Long facultyId, String facultyName, String name, String shortName,
                       String type, String officialUrl, String notes) {}
    record UniversityStatisticsRow(long universityId, String universityName, String universityAcronym,
                                   long campusCount, long facultyCount, long departmentCount, long programCount) {}
}
