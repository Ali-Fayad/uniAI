package com.uniai.chat.application.planning;

import java.util.List;

/** Route-specific argument DTOs. No route accepts an untyped catch-all map. */
public final class GraduateRouteArguments {
    private GraduateRouteArguments() {}

    public enum DegreeLevel { CERTIFICATE, DIPLOMA, MASTER, PHD }
    public enum BillingBasis { PER_CREDIT, PER_SEMESTER, PER_YEAR, PER_TERM, PER_PROGRAM, FLAT_FEE, PER_APPLICATION, PER_ACADEMIC_YEAR }
    public enum ScopeLevel { UNIVERSITY, FACULTY, DEPARTMENT, PROGRAM }
    public enum TuitionAggregation { AVG, MIN, MAX }
    public enum UniversityComparisonDimension { CAMPUS_COUNT, PROGRAM_COUNT, FACULTY_COUNT, DEPARTMENT_COUNT }
    public enum RequirementType { GENERAL, GRE, GMAT, ENGLISH, PORTFOLIO, INTERVIEW, EXPERIENCE, ACADEMIC, PREREQUISITE, OTHER }
    public enum DeadlineType { APPLICATION_OPEN, EARLY, PRIORITY, REGULAR, FINAL, INTERVIEW, ENROLLMENT, OTHER }

    public record DirectAiArguments(GraduateDirectAiReason reason) {}
    public record OverviewArguments(String university, DegreeLevel degreeType, Integer limit) {}

    public record ListUniversitiesArguments(String country, String city, Integer limit) {}
    public record SearchUniversitiesArguments(String query, String country, String city, Integer limit) {}
    public record UniversityArguments(String university) {}
    public record CountUniversitiesArguments(String country, String city) {}
    public record CityArguments(String city, Integer limit) {}
    public record CompareUniversitiesArguments(List<String> universities, UniversityComparisonDimension dimension) {}

    public record ListCampusesArguments(String university, String city, Integer limit) {}
    public record SearchCampusesArguments(String query, String university, String city, Integer limit) {}
    public record CampusArguments(String campusName, String university) {}
    public record CampusExistsArguments(String campusName, String university, String city) {}
    public record CountCampusesArguments(String university, String city) {}
    public record CompareCampusCountsArguments(List<String> universities, String city) {}

    public record ListFacultiesArguments(String university, String query, Integer limit) {}
    public record SearchFacultiesArguments(String query, String university, Integer limit) {}
    public record FacultyArguments(String university, String facultyName) {}
    public record CountFacultiesArguments(String university) {}
    public record ListDepartmentsArguments(String university, String facultyName, String query, Integer limit) {}
    public record SearchDepartmentsArguments(String query, String university, String facultyName, Integer limit) {}
    public record DepartmentArguments(String university, String departmentName, String facultyName) {}
    public record CountDepartmentsArguments(String university, String facultyName) {}

    public record ListProgramsArguments(String university, DegreeLevel degreeType, String facultyName,
                                        String departmentName, String city, String language, Integer limit) {}
    public record SearchProgramsArguments(String query, String university, DegreeLevel degreeType,
                                          String facultyName, String departmentName, String language, Integer limit) {}
    public record ProgramExistsArguments(String university, String programName, DegreeLevel degreeType) {}
    public record ProgramArguments(String university, String programName, DegreeLevel degreeType) {}
    public record ProgramSearchArguments(String university, String programName, DegreeLevel degreeType, Integer limit) {}
    public record CountProgramsArguments(String university, DegreeLevel degreeType, String facultyName,
                                         String departmentName, String city, String language) {}
    public record ProgramGroupCountArguments(String university, DegreeLevel degreeType, String query, Integer limit) {}
    public record CompareProgramsArguments(List<String> universities, String programName, DegreeLevel degreeType) {}
    public record CompareProgramCountsArguments(List<String> universities, DegreeLevel degreeType, String facultyName) {}

    public record TuitionArguments(String university, String programName, DegreeLevel degreeType,
                                   String facultyName, String departmentName, String academicYear,
                                   String currency, BillingBasis billingBasis, ScopeLevel scopeLevel, Integer limit) {}
    public record ProgramTuitionArguments(String university, String programName, DegreeLevel degreeType,
                                          String academicYear, String currency, BillingBasis billingBasis, Integer limit) {}
    public record FacultyTuitionArguments(String university, String facultyName, DegreeLevel degreeType,
                                          String academicYear, String currency, BillingBasis billingBasis, Integer limit) {}
    public record DepartmentTuitionArguments(String university, String departmentName, DegreeLevel degreeType,
                                             String academicYear, String currency, BillingBasis billingBasis, Integer limit) {}
    public record UniversityTuitionArguments(String university, DegreeLevel degreeType, String academicYear,
                                             String currency, BillingBasis billingBasis, Integer limit) {}
    public record TuitionAggregateArguments(List<String> universities, String programName, DegreeLevel degreeType,
                                            String academicYear, String currency, BillingBasis billingBasis,
                                            ScopeLevel scopeLevel) {}
    public record CompareTuitionArguments(List<String> universities, String programName, DegreeLevel degreeType,
                                          String academicYear, String currency, BillingBasis billingBasis,
                                          TuitionAggregation aggregation) {}
    public record FeeArguments(String university, String programName, String facultyName, String departmentName,
                               String academicYear, String currency, Integer limit) {}

    public record RequirementArguments(String university, String programName, String facultyName,
                                       String departmentName, DegreeLevel degreeType,
                                       RequirementType requirementType, Integer limit) {}
    public record LanguageRequirementArguments(String university, String programName,
                                               DegreeLevel degreeType, Integer limit) {}
    public record TestRequirementArguments(String university, String programName, DegreeLevel degreeType,
                                           List<RequirementType> tests, Integer limit) {}
    public record DocumentArguments(String university, String programName, String facultyName,
                                    String departmentName, String documentType, Boolean requiredOnly, Integer limit) {}
    public record DeadlineArguments(String university, String programName, String facultyName,
                                    String departmentName, String academicYear, DeadlineType deadlineType,
                                    String term, Integer limit) {}
    public record RequirementExistsArguments(String university, RequirementType requirementType,
                                             String programName, DegreeLevel degreeType) {}
    public record CompareRequirementArguments(List<String> universities, String programName,
                                              DegreeLevel degreeType, List<RequirementType> requirementTypes) {}

    public record ScholarshipArguments(String university, String programName, String facultyName,
                                       String departmentName, String academicYear, String currency, Integer limit) {}
    public record FinancialAidArguments(String university, String programName, String facultyName,
                                        String departmentName, String academicYear, String currency, Integer limit) {}
    public record PaymentPlanArguments(String university, String programName, String facultyName,
                                       String departmentName, String academicYear, Integer limit) {}
    public record AccreditationArguments(String university, String programName, String facultyName,
                                         String departmentName, String status, Integer limit) {}
}
