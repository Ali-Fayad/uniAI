package com.uniai.chat.application.planning;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.uniai.chat.application.planning.GraduateRouteArguments.*;

/**
 * Explicit source of truth for routes exposed to the planner.
 *
 * <p>The registry is intentionally Java-owned: an AI response cannot add a route,
 * alter its argument type, or increase its result limit.</p>
 */
public final class GraduateAiRouteCatalog {
    private static final int DEFAULT_LIST_LIMIT = 50;
    private static final int COMPARISON_LIMIT = 10;
    private final Map<GraduateAiRoute, GraduateAiRouteDefinition<?>> definitions;

    public GraduateAiRouteCatalog() {
        EnumMap<GraduateAiRoute, GraduateAiRouteDefinition<?>> routes = new EnumMap<>(GraduateAiRoute.class);

        add(routes, GraduateAiRoute.DIRECT_AI_RESPONSE, "Continue without graduate database retrieval",
                DirectAiArguments.class, List.of("reason"), 0);
        add(routes, GraduateAiRoute.GET_GRADUATE_OVERVIEW, "Summarize available graduate knowledge",
                OverviewArguments.class, List.of(), 25);

        add(routes, GraduateAiRoute.LIST_UNIVERSITIES, "List universities", ListUniversitiesArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.SEARCH_UNIVERSITIES, "Search universities by name or acronym", SearchUniversitiesArguments.class, List.of("query"), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.GET_UNIVERSITY_DETAILS, "Get one university's details", UniversityArguments.class, List.of("university"), 1);
        add(routes, GraduateAiRoute.COUNT_UNIVERSITIES, "Count universities", CountUniversitiesArguments.class, List.of(), 1);
        add(routes, GraduateAiRoute.LIST_UNIVERSITIES_BY_CITY, "List universities with campuses in a city", CityArguments.class, List.of("city"), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.COMPARE_UNIVERSITIES, "Compare selected universities", CompareUniversitiesArguments.class, List.of("universities", "dimension"), COMPARISON_LIMIT);

        add(routes, GraduateAiRoute.LIST_CAMPUSES, "List campuses", ListCampusesArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.SEARCH_CAMPUSES, "Search campuses", SearchCampusesArguments.class, List.of("query"), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.GET_CAMPUS_DETAILS, "Get one campus's details", CampusArguments.class, List.of("campusName"), 1);
        add(routes, GraduateAiRoute.CHECK_CAMPUS_EXISTS, "Check whether a matching campus exists", CampusExistsArguments.class, List.of(), 1);
        add(routes, GraduateAiRoute.COUNT_CAMPUSES, "Count campuses", CountCampusesArguments.class, List.of(), 1);
        add(routes, GraduateAiRoute.COMPARE_CAMPUS_COUNTS, "Compare campus counts", CompareCampusCountsArguments.class, List.of("universities"), COMPARISON_LIMIT);

        add(routes, GraduateAiRoute.LIST_FACULTIES, "List faculties or schools", ListFacultiesArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.SEARCH_FACULTIES, "Search faculties or schools", SearchFacultiesArguments.class, List.of("query"), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.GET_FACULTY_DETAILS, "Get one faculty's details", FacultyArguments.class, List.of("facultyName"), 1);
        add(routes, GraduateAiRoute.COUNT_FACULTIES, "Count faculties", CountFacultiesArguments.class, List.of(), 1);
        add(routes, GraduateAiRoute.LIST_DEPARTMENTS, "List departments", ListDepartmentsArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.SEARCH_DEPARTMENTS, "Search departments", SearchDepartmentsArguments.class, List.of("query"), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.GET_DEPARTMENT_DETAILS, "Get one department's details", DepartmentArguments.class, List.of("departmentName"), 1);
        add(routes, GraduateAiRoute.COUNT_DEPARTMENTS, "Count departments", CountDepartmentsArguments.class, List.of(), 1);

        add(routes, GraduateAiRoute.LIST_PROGRAMS, "List graduate programs", ListProgramsArguments.class, List.of(), 200);
        add(routes, GraduateAiRoute.SEARCH_PROGRAMS, "Search graduate programs", SearchProgramsArguments.class, List.of("query"), 200);
        add(routes, GraduateAiRoute.CHECK_PROGRAM_EXISTS, "Check whether a graduate program exists", ProgramExistsArguments.class, List.of("programName"), 1);
        add(routes, GraduateAiRoute.GET_PROGRAM_DETAILS, "Get one graduate program's details", ProgramArguments.class, List.of("programName"), 1);
        add(routes, GraduateAiRoute.LIST_PROGRAM_TRACKS, "List a program's tracks", ProgramSearchArguments.class, List.of("programName"), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.LIST_PROGRAM_LANGUAGES, "List program languages", ProgramSearchArguments.class, List.of("programName"), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.LIST_PROGRAM_SOURCES, "List program evidence sources", ProgramSearchArguments.class, List.of("programName"), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.COUNT_PROGRAMS, "Count graduate programs", CountProgramsArguments.class, List.of(), 1);
        add(routes, GraduateAiRoute.COUNT_PROGRAMS_BY_UNIVERSITY, "Count programs grouped by university", ProgramGroupCountArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.COUNT_PROGRAMS_BY_FACULTY, "Count programs grouped by faculty", ProgramGroupCountArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.COUNT_PROGRAMS_BY_DEPARTMENT, "Count programs grouped by department", ProgramGroupCountArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.COMPARE_PROGRAM_AVAILABILITY, "Compare program availability", CompareProgramsArguments.class, List.of("universities", "programName"), COMPARISON_LIMIT);
        add(routes, GraduateAiRoute.COMPARE_PROGRAM_COUNTS, "Compare program counts", CompareProgramCountsArguments.class, List.of("universities"), COMPARISON_LIMIT);

        add(routes, GraduateAiRoute.GET_TUITION, "Get matching tuition rates", TuitionArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.GET_PROGRAM_TUITION, "Get tuition for a program", ProgramTuitionArguments.class, List.of("programName"), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.GET_FACULTY_TUITION, "Get faculty-scoped tuition", FacultyTuitionArguments.class, List.of("facultyName"), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.GET_DEPARTMENT_TUITION, "Get department-scoped tuition", DepartmentTuitionArguments.class, List.of("departmentName"), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.GET_UNIVERSITY_TUITION, "Get university-scoped tuition", UniversityTuitionArguments.class, List.of("university"), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.GET_MINIMUM_TUITION, "Get minimum matching tuition", TuitionAggregateArguments.class, List.of(), 100);
        add(routes, GraduateAiRoute.GET_MAXIMUM_TUITION, "Get maximum matching tuition", TuitionAggregateArguments.class, List.of(), 100);
        add(routes, GraduateAiRoute.GET_AVERAGE_TUITION, "Get average matching tuition", TuitionAggregateArguments.class, List.of(), 100);
        add(routes, GraduateAiRoute.COMPARE_TUITION, "Compare tuition", CompareTuitionArguments.class, List.of("universities"), 100);
        add(routes, GraduateAiRoute.LIST_FEE_ITEMS, "List non-tuition fee items", FeeArguments.class, List.of(), DEFAULT_LIST_LIMIT);

        add(routes, GraduateAiRoute.GET_ADMISSION_REQUIREMENTS, "Get admission requirements", RequirementArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.GET_LANGUAGE_REQUIREMENTS, "Get language admission requirements", LanguageRequirementArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.GET_TEST_REQUIREMENTS, "Get test and score requirements", TestRequirementArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.LIST_REQUIRED_DOCUMENTS, "List required application documents", DocumentArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.GET_APPLICATION_DEADLINES, "Get application deadlines", DeadlineArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.CHECK_ADMISSION_REQUIREMENT, "Check whether an admission requirement applies", RequirementExistsArguments.class, List.of("requirementType"), 1);
        add(routes, GraduateAiRoute.COMPARE_ADMISSION_REQUIREMENTS, "Compare admission requirements", CompareRequirementArguments.class, List.of("universities"), 100);

        add(routes, GraduateAiRoute.LIST_SCHOLARSHIPS, "List graduate scholarships", ScholarshipArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.LIST_FINANCIAL_AID, "List graduate financial aid", FinancialAidArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.LIST_PAYMENT_PLANS, "List graduate payment plans", PaymentPlanArguments.class, List.of(), DEFAULT_LIST_LIMIT);
        add(routes, GraduateAiRoute.LIST_ACCREDITATIONS, "List graduate accreditations", AccreditationArguments.class, List.of(), DEFAULT_LIST_LIMIT);

        if (routes.size() != GraduateAiRoute.values().length) {
            throw new IllegalStateException("Every GraduateAiRoute must have exactly one catalog definition");
        }
        definitions = Collections.unmodifiableMap(routes);
    }

    public GraduateAiRouteDefinition<?> definition(GraduateAiRoute route) {
        GraduateAiRouteDefinition<?> definition = definitions.get(route);
        if (definition == null) {
            throw new GraduateRoutePlanningException("Unsupported graduate route: " + route);
        }
        return definition;
    }

    public Map<GraduateAiRoute, GraduateAiRouteDefinition<?>> definitions() {
        return definitions;
    }

    private <T> void add(Map<GraduateAiRoute, GraduateAiRouteDefinition<?>> routes,
                         GraduateAiRoute route,
                         String purpose,
                         Class<T> argumentType,
                         List<String> requiredArguments,
                         int maximumResultLimit) {
        GraduateAiRouteDefinition<T> previous = (GraduateAiRouteDefinition<T>) routes.put(
                route,
                new GraduateAiRouteDefinition<>(route, purpose, argumentType, requiredArguments, maximumResultLimit, true));
        if (previous != null) {
            throw new IllegalStateException("Duplicate graduate route definition: " + route);
        }
    }
}
