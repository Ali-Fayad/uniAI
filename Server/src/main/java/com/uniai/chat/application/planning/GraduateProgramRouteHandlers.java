package com.uniai.chat.application.planning;

import com.uniai.chat.application.port.out.GraduateProgramRouteDao;

import java.util.List;

import static com.uniai.chat.application.planning.GraduateRouteArguments.*;

/** Explicit handler registrations for the implemented program route family. */
public final class GraduateProgramRouteHandlers {
    private GraduateProgramRouteHandlers() {}

    public static List<GraduateAiRouteHandler<?>> create(GraduateProgramRouteDao dao) {
        return List.of(
                handler(GraduateAiRoute.LIST_PROGRAMS, ListProgramsArguments.class, dao),
                handler(GraduateAiRoute.SEARCH_PROGRAMS, SearchProgramsArguments.class, dao),
                handler(GraduateAiRoute.CHECK_PROGRAM_EXISTS, ProgramExistsArguments.class, dao),
                handler(GraduateAiRoute.GET_PROGRAM_DETAILS, ProgramArguments.class, dao),
                handler(GraduateAiRoute.LIST_PROGRAM_TRACKS, ProgramSearchArguments.class, dao),
                handler(GraduateAiRoute.LIST_PROGRAM_LANGUAGES, ProgramSearchArguments.class, dao),
                handler(GraduateAiRoute.LIST_PROGRAM_SOURCES, ProgramSearchArguments.class, dao),
                handler(GraduateAiRoute.COUNT_PROGRAMS, CountProgramsArguments.class, dao),
                handler(GraduateAiRoute.COUNT_PROGRAMS_BY_UNIVERSITY, ProgramGroupCountArguments.class, dao),
                handler(GraduateAiRoute.COUNT_PROGRAMS_BY_FACULTY, ProgramGroupCountArguments.class, dao),
                handler(GraduateAiRoute.COUNT_PROGRAMS_BY_DEPARTMENT, ProgramGroupCountArguments.class, dao),
                handler(GraduateAiRoute.COMPARE_PROGRAM_AVAILABILITY, CompareProgramsArguments.class, dao),
                handler(GraduateAiRoute.COMPARE_PROGRAM_COUNTS, CompareProgramCountsArguments.class, dao));
    }

    private static <T> GraduateAiRouteHandler<T> handler(
            GraduateAiRoute route, Class<T> argumentType, GraduateProgramRouteDao dao) {
        return new GraduateProgramRouteHandler<>(route, argumentType, dao);
    }
}
