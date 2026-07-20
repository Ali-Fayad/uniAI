package com.uniai.chat.application.planning;

import com.uniai.chat.application.citation.GraduateCitation;
import com.uniai.chat.application.port.out.GraduateProgramRouteDao;
import com.uniai.chat.application.port.out.GraduateProgramRouteDao.GroupCountRow;
import com.uniai.chat.application.port.out.GraduateProgramRouteDao.ProgramCriteria;
import com.uniai.chat.application.port.out.GraduateProgramRouteDao.ProgramEvidenceRow;
import com.uniai.chat.application.port.out.GraduateProgramRouteDao.ProgramGrouping;
import com.uniai.chat.application.port.out.GraduateProgramRouteDao.ProgramPage;
import com.uniai.chat.application.port.out.GraduateProgramRouteDao.ProgramRow;
import com.uniai.chat.application.retrieval.ResolvedUniversity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/** Route-specific execution adapter for program, track, language, source, and program-count routes. */
public final class GraduateProgramRouteHandler<T> implements GraduateAiRouteHandler<T> {
    private static final int DEFAULT_LIST_LIMIT = 200;
    private static final int DEFAULT_DETAIL_LIMIT = 20;
    private final GraduateAiRoute route;
    private final Class<T> argumentType;
    private final GraduateProgramRouteDao dao;

    public GraduateProgramRouteHandler(GraduateAiRoute route, Class<T> argumentType, GraduateProgramRouteDao dao) {
        this.route = route;
        this.argumentType = argumentType;
        this.dao = dao;
    }

    @Override
    public GraduateAiRoute route() {
        return route;
    }

    @Override
    public Class<T> argumentType() {
        return argumentType;
    }

    @Override
    public GraduateRouteExecutionResult execute(T arguments) {
        throw new GraduateRoutePlanningException("Program routes require resolved entity context");
    }

    @Override
    public GraduateRouteExecutionResult executeResolved(ResolvedGraduateRoutePlan<T> plan) {
        ProgramCriteria criteria = criteria(plan.arguments(), plan.universities());
        return switch (route) {
            case LIST_PROGRAMS, SEARCH_PROGRAMS, GET_PROGRAM_DETAILS -> pageResult(plan, dao.findPrograms(criteria));
            case CHECK_PROGRAM_EXISTS -> scalarResult(plan, "Program exists", dao.countPrograms(criteria) > 0);
            case COUNT_PROGRAMS -> scalarResult(plan, "Program count", dao.countPrograms(criteria));
            case COUNT_PROGRAMS_BY_UNIVERSITY -> groupedResult(plan,
                    dao.countProgramsBy(criteria, ProgramGrouping.UNIVERSITY));
            case COUNT_PROGRAMS_BY_FACULTY -> groupedResult(plan,
                    dao.countProgramsBy(criteria, ProgramGrouping.FACULTY));
            case COUNT_PROGRAMS_BY_DEPARTMENT -> groupedResult(plan,
                    dao.countProgramsBy(criteria, ProgramGrouping.DEPARTMENT));
            case COMPARE_PROGRAM_AVAILABILITY -> availabilityResult(plan, dao.findPrograms(criteria));
            case COMPARE_PROGRAM_COUNTS -> groupedResult(plan,
                    dao.countProgramsBy(criteria, ProgramGrouping.UNIVERSITY));
            case LIST_PROGRAM_TRACKS -> evidenceResult(plan, dao.findTracks(criteria));
            case LIST_PROGRAM_LANGUAGES -> evidenceResult(plan, dao.findLanguages(criteria));
            case LIST_PROGRAM_SOURCES -> evidenceResult(plan, dao.findSources(criteria));
            default -> throw new GraduateRoutePlanningException("Unsupported program route handler: " + route);
        };
    }

    private ProgramCriteria criteria(Object arguments, List<ResolvedUniversity> universities) {
        Values values = Values.from(arguments);
        return new ProgramCriteria(
                universities.stream().map(ResolvedUniversity::id).filter(Objects::nonNull).toList(),
                values.searchQuery,
                values.programName,
                values.degreeLevel,
                values.facultyName,
                values.departmentName,
                values.language,
                values.city,
                values.limit == null ? defaultLimit() : values.limit);
    }

    private int defaultLimit() {
        return switch (route) {
            case LIST_PROGRAMS, SEARCH_PROGRAMS -> DEFAULT_LIST_LIMIT;
            case GET_PROGRAM_DETAILS -> 1;
            default -> DEFAULT_DETAIL_LIMIT;
        };
    }

    private GraduateRouteExecutionResult pageResult(ResolvedGraduateRoutePlan<T> plan, ProgramPage page) {
        StringBuilder context = new StringBuilder("Program results:\n")
                .append("Total matching programs: ").append(page.totalMatches()).append('\n')
                .append("Returned programs: ").append(page.rows().size()).append('\n');
        int ordinal = 1;
        for (ProgramRow row : page.rows()) {
            context.append(ordinal++).append(". University: ").append(row.universityName());
            if (text(row.universityAcronym())) context.append(" (").append(row.universityAcronym()).append(')');
            context.append(" | Program: ").append(displayName(row));
            append(context, "Degree", row.degreeType());
            append(context, "Faculty", row.facultyName());
            append(context, "Department", row.departmentName());
            if (row.credits() != null) append(context, "Credits", row.credits().toString());
            append(context, "Duration", row.duration());
            append(context, "Language", row.language());
            append(context, "Delivery", row.deliveryMode());
            if (route == GraduateAiRoute.GET_PROGRAM_DETAILS) append(context, "Description", row.description());
            context.append('\n');
        }
        List<String> warnings = page.truncated()
                ? List.of("The configured route limit was applied; " + page.totalMatches() + " rows matched but "
                + page.rows().size() + " were returned.") : List.of();
        return result(plan, context.toString().trim(), citations(page.rows()), warnings, page.rows().isEmpty());
    }

    private GraduateRouteExecutionResult availabilityResult(ResolvedGraduateRoutePlan<T> plan, ProgramPage page) {
        var foundIds = page.rows().stream().map(ProgramRow::universityId).collect(Collectors.toSet());
        StringBuilder context = new StringBuilder("Program availability comparison:\n");
        for (ResolvedUniversity university : plan.universities()) {
            context.append("- ").append(university.name()).append(": ")
                    .append(foundIds.contains(university.id())).append('\n');
        }
        return result(plan, context.toString().trim(), citations(page.rows()), List.of(), false);
    }

    private GraduateRouteExecutionResult groupedResult(ResolvedGraduateRoutePlan<T> plan, List<GroupCountRow> rows) {
        StringBuilder context = new StringBuilder("Grouped program counts:\n");
        for (GroupCountRow row : rows) {
            context.append("- ").append(row.groupName()).append(" | University: ")
                    .append(row.universityName()).append(" | Count: ").append(row.count()).append('\n');
        }
        return result(plan, context.toString().trim(), List.of(), List.of(), rows.isEmpty());
    }

    private GraduateRouteExecutionResult evidenceResult(ResolvedGraduateRoutePlan<T> plan,
                                                        List<ProgramEvidenceRow> rows) {
        StringBuilder context = new StringBuilder(route.name().replace('_', ' ').toLowerCase(Locale.ROOT)).append(":\n");
        int ordinal = 1;
        for (ProgramEvidenceRow row : rows) {
            context.append(ordinal++).append(". University: ").append(row.universityName())
                    .append(" | Program: ").append(row.programName())
                    .append(" | Type: ").append(row.kind())
                    .append(" | Value: ").append(row.value());
            append(context, "Details", row.details());
            context.append('\n');
        }
        return result(plan, context.toString().trim(), evidenceCitations(rows), List.of(), rows.isEmpty());
    }

    private GraduateRouteExecutionResult scalarResult(ResolvedGraduateRoutePlan<T> plan, String label, Object value) {
        return result(plan, label + ": " + value, List.of(), List.of(), false);
    }

    private GraduateRouteExecutionResult result(ResolvedGraduateRoutePlan<T> plan,
                                                String context,
                                                List<GraduateCitation> citations,
                                                List<String> warnings,
                                                boolean empty) {
        return new GraduateRouteExecutionResult(route, plan.canonicalArguments(), context,
                citations, warnings, empty, null);
    }

    private List<GraduateCitation> citations(List<ProgramRow> rows) {
        List<GraduateCitation> citations = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            ProgramRow row = rows.get(i);
            String url = text(row.sourceUrl()) ? row.sourceUrl() : row.officialUrl();
            if (!text(url)) continue;
            citations.add(new GraduateCitation("route-program-" + row.programId(), "S" + (i + 1),
                    text(row.sourceTitle()) ? row.sourceTitle() : displayName(row), url, "PROGRAM",
                    row.universityId(), row.universityName(), row.programId(), displayName(row)));
        }
        return List.copyOf(citations);
    }

    private List<GraduateCitation> evidenceCitations(List<ProgramEvidenceRow> rows) {
        List<GraduateCitation> citations = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            ProgramEvidenceRow row = rows.get(i);
            if (!text(row.sourceUrl())) continue;
            citations.add(new GraduateCitation("route-evidence-" + route + "-" + row.programId() + "-" + i,
                    "S" + (i + 1), row.sourceTitle(), row.sourceUrl(), route.name(), row.universityId(),
                    row.universityName(), row.programId(), row.programName()));
        }
        return List.copyOf(citations);
    }

    private String displayName(ProgramRow row) {
        if (text(row.officialDegreeName())) return row.officialDegreeName();
        if (text(row.major())) return row.major();
        return row.programKey();
    }

    private void append(StringBuilder builder, String label, String value) {
        if (text(value)) builder.append(" | ").append(label).append(": ").append(value.replace('\n', ' '));
    }

    private boolean text(String value) {
        return value != null && !value.isBlank();
    }

    /** Internal extraction only; the external contract remains one dedicated record per route. */
    private static final class Values {
        String searchQuery;
        String programName;
        String degreeLevel;
        String facultyName;
        String departmentName;
        String language;
        String city;
        Integer limit;

        static Values from(Object value) {
            Values result = new Values();
            if (value instanceof GraduateRouteArguments.ListProgramsArguments a) {
                result.degreeLevel = name(a.degreeType()); result.facultyName = a.facultyName();
                result.departmentName = a.departmentName(); result.language = a.language(); result.city = a.city(); result.limit = a.limit();
            } else if (value instanceof GraduateRouteArguments.SearchProgramsArguments a) {
                result.searchQuery = a.query(); result.degreeLevel = name(a.degreeType()); result.facultyName = a.facultyName();
                result.departmentName = a.departmentName(); result.language = a.language(); result.limit = a.limit();
            } else if (value instanceof GraduateRouteArguments.ProgramExistsArguments a) {
                result.programName = a.programName(); result.degreeLevel = name(a.degreeType());
            } else if (value instanceof GraduateRouteArguments.ProgramArguments a) {
                result.programName = a.programName(); result.degreeLevel = name(a.degreeType());
            } else if (value instanceof GraduateRouteArguments.ProgramSearchArguments a) {
                result.programName = a.programName(); result.degreeLevel = name(a.degreeType()); result.limit = a.limit();
            } else if (value instanceof GraduateRouteArguments.CountProgramsArguments a) {
                result.degreeLevel = name(a.degreeType()); result.facultyName = a.facultyName();
                result.departmentName = a.departmentName(); result.language = a.language(); result.city = a.city();
            } else if (value instanceof GraduateRouteArguments.ProgramGroupCountArguments a) {
                result.degreeLevel = name(a.degreeType()); result.searchQuery = a.query(); result.limit = a.limit();
            } else if (value instanceof GraduateRouteArguments.CompareProgramsArguments a) {
                result.programName = a.programName(); result.degreeLevel = name(a.degreeType()); result.limit = 200;
            } else if (value instanceof GraduateRouteArguments.CompareProgramCountsArguments a) {
                result.degreeLevel = name(a.degreeType()); result.facultyName = a.facultyName(); result.limit = 200;
            } else {
                throw new GraduateRoutePlanningException("Unsupported program argument type: " + value.getClass().getSimpleName());
            }
            return result;
        }

        private static String name(Enum<?> value) {
            return value == null ? null : value.name();
        }
    }
}
