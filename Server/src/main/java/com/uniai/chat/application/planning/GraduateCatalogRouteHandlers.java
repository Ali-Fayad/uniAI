package com.uniai.chat.application.planning;

import com.uniai.chat.application.port.out.GraduateCatalogRouteDao;
import java.util.List;
import static com.uniai.chat.application.planning.GraduateRouteArguments.*;

public final class GraduateCatalogRouteHandlers {
    private GraduateCatalogRouteHandlers(){}
    public static List<GraduateAiRouteHandler<?>> create(GraduateCatalogRouteDao dao){return List.of(
            h(GraduateAiRoute.GET_GRADUATE_OVERVIEW,OverviewArguments.class,dao),
            h(GraduateAiRoute.LIST_UNIVERSITIES,ListUniversitiesArguments.class,dao),h(GraduateAiRoute.SEARCH_UNIVERSITIES,SearchUniversitiesArguments.class,dao),
            h(GraduateAiRoute.GET_UNIVERSITY_DETAILS,UniversityArguments.class,dao),h(GraduateAiRoute.COUNT_UNIVERSITIES,CountUniversitiesArguments.class,dao),
            h(GraduateAiRoute.LIST_UNIVERSITIES_BY_CITY,CityArguments.class,dao),h(GraduateAiRoute.COMPARE_UNIVERSITIES,CompareUniversitiesArguments.class,dao),
            h(GraduateAiRoute.LIST_CAMPUSES,ListCampusesArguments.class,dao),h(GraduateAiRoute.SEARCH_CAMPUSES,SearchCampusesArguments.class,dao),
            h(GraduateAiRoute.GET_CAMPUS_DETAILS,CampusArguments.class,dao),h(GraduateAiRoute.CHECK_CAMPUS_EXISTS,CampusExistsArguments.class,dao),
            h(GraduateAiRoute.COUNT_CAMPUSES,CountCampusesArguments.class,dao),h(GraduateAiRoute.COMPARE_CAMPUS_COUNTS,CompareCampusCountsArguments.class,dao),
            h(GraduateAiRoute.LIST_FACULTIES,ListFacultiesArguments.class,dao),h(GraduateAiRoute.SEARCH_FACULTIES,SearchFacultiesArguments.class,dao),
            h(GraduateAiRoute.GET_FACULTY_DETAILS,FacultyArguments.class,dao),h(GraduateAiRoute.COUNT_FACULTIES,CountFacultiesArguments.class,dao),
            h(GraduateAiRoute.LIST_DEPARTMENTS,ListDepartmentsArguments.class,dao),h(GraduateAiRoute.SEARCH_DEPARTMENTS,SearchDepartmentsArguments.class,dao),
            h(GraduateAiRoute.GET_DEPARTMENT_DETAILS,DepartmentArguments.class,dao),h(GraduateAiRoute.COUNT_DEPARTMENTS,CountDepartmentsArguments.class,dao));}
    private static <T> GraduateAiRouteHandler<T> h(GraduateAiRoute r,Class<T> c,GraduateCatalogRouteDao d){return new GraduateCatalogRouteHandler<>(r,c,d);}
}
