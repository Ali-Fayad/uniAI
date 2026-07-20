package com.uniai.chat.application.planning;

import com.uniai.chat.application.port.out.GraduateTuitionRouteDao;

import java.util.List;

import static com.uniai.chat.application.planning.GraduateRouteArguments.*;

public final class GraduateTuitionRouteHandlers {
    private GraduateTuitionRouteHandlers() {}

    public static List<GraduateAiRouteHandler<?>> create(GraduateTuitionRouteDao dao) {
        return List.of(
                handler(GraduateAiRoute.GET_TUITION, TuitionArguments.class, dao),
                handler(GraduateAiRoute.GET_PROGRAM_TUITION, ProgramTuitionArguments.class, dao),
                handler(GraduateAiRoute.GET_FACULTY_TUITION, FacultyTuitionArguments.class, dao),
                handler(GraduateAiRoute.GET_DEPARTMENT_TUITION, DepartmentTuitionArguments.class, dao),
                handler(GraduateAiRoute.GET_UNIVERSITY_TUITION, UniversityTuitionArguments.class, dao),
                handler(GraduateAiRoute.GET_MINIMUM_TUITION, TuitionAggregateArguments.class, dao),
                handler(GraduateAiRoute.GET_MAXIMUM_TUITION, TuitionAggregateArguments.class, dao),
                handler(GraduateAiRoute.GET_AVERAGE_TUITION, TuitionAggregateArguments.class, dao),
                handler(GraduateAiRoute.COMPARE_TUITION, CompareTuitionArguments.class, dao),
                handler(GraduateAiRoute.RANK_UNIVERSITIES_BY_TUITION, RankUniversitiesByTuitionArguments.class, dao),
                handler(GraduateAiRoute.RANK_PROGRAMS_BY_TUITION, RankProgramsByTuitionArguments.class, dao),
                handler(GraduateAiRoute.LIST_FEE_ITEMS, FeeArguments.class, dao));
    }

    private static <T> GraduateAiRouteHandler<T> handler(
            GraduateAiRoute route, Class<T> argumentType, GraduateTuitionRouteDao dao) {
        return new GraduateTuitionRouteHandler<>(route, argumentType, dao);
    }
}
