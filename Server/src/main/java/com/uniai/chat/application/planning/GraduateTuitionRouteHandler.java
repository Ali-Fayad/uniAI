package com.uniai.chat.application.planning;

import com.uniai.chat.application.citation.GraduateCitation;
import com.uniai.chat.application.port.out.GraduateTuitionRouteDao;
import com.uniai.chat.application.port.out.GraduateTuitionRouteDao.FeePage;
import com.uniai.chat.application.port.out.GraduateTuitionRouteDao.FeeRow;
import com.uniai.chat.application.port.out.GraduateTuitionRouteDao.TuitionAggregateRow;
import com.uniai.chat.application.port.out.GraduateTuitionRouteDao.TuitionCriteria;
import com.uniai.chat.application.port.out.GraduateTuitionRouteDao.TuitionPage;
import com.uniai.chat.application.port.out.GraduateTuitionRouteDao.TuitionRow;
import com.uniai.chat.application.retrieval.ResolvedUniversity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Route execution for tuition, tuition analytics, comparison, and fee items. */
public final class GraduateTuitionRouteHandler<T> implements GraduateAiRouteHandler<T> {
    private final GraduateAiRoute route;
    private final Class<T> argumentType;
    private final GraduateTuitionRouteDao dao;

    public GraduateTuitionRouteHandler(GraduateAiRoute route, Class<T> argumentType, GraduateTuitionRouteDao dao) {
        this.route = route;
        this.argumentType = argumentType;
        this.dao = dao;
    }

    @Override public GraduateAiRoute route() { return route; }
    @Override public Class<T> argumentType() { return argumentType; }

    @Override
    public GraduateRouteExecutionResult execute(T arguments) {
        throw new GraduateRoutePlanningException("Tuition routes require resolved entity context");
    }

    @Override
    public GraduateRouteExecutionResult executeResolved(ResolvedGraduateRoutePlan<T> plan) {
        TuitionCriteria criteria = criteria(plan.arguments(), plan.universities());
        return switch (route) {
            case GET_TUITION, GET_PROGRAM_TUITION, GET_FACULTY_TUITION,
                    GET_DEPARTMENT_TUITION, GET_UNIVERSITY_TUITION -> rates(plan, dao.findTuition(criteria));
            case GET_MINIMUM_TUITION, GET_MAXIMUM_TUITION, GET_AVERAGE_TUITION,
                    COMPARE_TUITION -> aggregates(plan, dao.aggregateTuition(criteria));
            case LIST_FEE_ITEMS -> fees(plan, dao.findFees(criteria));
            default -> throw new GraduateRoutePlanningException("Unsupported tuition route: " + route);
        };
    }

    private TuitionCriteria criteria(Object arguments, List<ResolvedUniversity> universities) {
        Values values = Values.from(arguments);
        String forcedScope = switch (route) {
            case GET_FACULTY_TUITION -> "FACULTY";
            case GET_DEPARTMENT_TUITION -> "DEPARTMENT";
            case GET_UNIVERSITY_TUITION -> "UNIVERSITY";
            default -> values.scopeLevel;
        };
        return new TuitionCriteria(
                universities.stream().map(ResolvedUniversity::id).filter(Objects::nonNull).toList(),
                values.programName, values.degreeLevel, values.facultyName, values.departmentName,
                values.academicYear, values.currency, values.billingBasis, forcedScope,
                values.limit == null ? 50 : values.limit);
    }

    private GraduateRouteExecutionResult rates(ResolvedGraduateRoutePlan<T> plan, TuitionPage page) {
        StringBuilder context = new StringBuilder("Tuition rates:\n")
                .append("Total matching rates: ").append(page.totalMatches()).append('\n')
                .append("Returned rates: ").append(page.rows().size()).append('\n');
        int ordinal = 1;
        for (TuitionRow row : page.rows()) {
            context.append(ordinal++).append(". University: ").append(row.universityName());
            append(context, "Program", row.programName());
            append(context, "Degree", row.degreeType());
            append(context, "Faculty", row.facultyName());
            append(context, "Department", row.departmentName());
            append(context, "Amount", money(row.amount(), row.currency()));
            append(context, "Billing basis", row.billingBasis());
            append(context, "Scope level", row.scopeLevel());
            append(context, "Academic year", row.academicYear());
            append(context, "Category", row.category());
            context.append('\n');
        }
        List<String> warnings = page.truncated()
                ? List.of("The configured tuition route limit was applied; not all matching rates are shown.") : List.of();
        return result(plan, context.toString().trim(), rateCitations(page.rows()), warnings, page.rows().isEmpty());
    }

    private GraduateRouteExecutionResult aggregates(ResolvedGraduateRoutePlan<T> plan,
                                                     List<TuitionAggregateRow> rows) {
        StringBuilder context = new StringBuilder("Tuition analytics:\n");
        for (TuitionAggregateRow row : rows) {
            context.append("- University: ").append(row.universityName());
            append(context, "Academic year", row.academicYear());
            append(context, "Currency", row.currency());
            append(context, "Billing basis", row.billingBasis());
            append(context, "Scope level", row.scopeLevel());
            append(context, "Records", String.valueOf(row.recordCount()));
            if (route == GraduateAiRoute.GET_MINIMUM_TUITION) append(context, "Minimum", money(row.minimumAmount(), row.currency()));
            else if (route == GraduateAiRoute.GET_MAXIMUM_TUITION) append(context, "Maximum", money(row.maximumAmount(), row.currency()));
            else append(context, "Average", money(row.averageAmount(), row.currency()));
            context.append('\n');
        }
        return result(plan, context.toString().trim(), List.of(), List.of(), rows.isEmpty());
    }

    private GraduateRouteExecutionResult fees(ResolvedGraduateRoutePlan<T> plan, FeePage page) {
        StringBuilder context = new StringBuilder("Graduate fee items:\n");
        for (FeeRow row : page.rows()) {
            context.append("- University: ").append(row.universityName());
            append(context, "Fee", row.feeName());
            append(context, "Amount", money(row.amount(), row.currency()));
            append(context, "Billing basis", row.billingBasis());
            append(context, "Scope level", row.scopeLevel());
            append(context, "Academic year", row.academicYear());
            append(context, "Program", row.programName());
            context.append('\n');
        }
        List<String> warnings = page.truncated()
                ? List.of("The configured fee route limit was applied; not all matching fee items are shown.") : List.of();
        return result(plan, context.toString().trim(), feeCitations(page.rows()), warnings, page.rows().isEmpty());
    }

    private GraduateRouteExecutionResult result(ResolvedGraduateRoutePlan<T> plan, String context,
                                                List<GraduateCitation> citations, List<String> warnings, boolean empty) {
        return new GraduateRouteExecutionResult(route, plan.canonicalArguments(), context,
                citations, warnings, empty, null);
    }

    private List<GraduateCitation> rateCitations(List<TuitionRow> rows) {
        List<GraduateCitation> citations = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            TuitionRow row = rows.get(i);
            if (!text(row.sourceUrl())) continue;
            citations.add(new GraduateCitation("route-tuition-" + row.tuitionId(), "S" + (i + 1),
                    row.sourceTitle(), row.sourceUrl(), "TUITION", row.universityId(), row.universityName(),
                    row.programId(), row.programName()));
        }
        return List.copyOf(citations);
    }

    private List<GraduateCitation> feeCitations(List<FeeRow> rows) {
        List<GraduateCitation> citations = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            FeeRow row = rows.get(i);
            if (!text(row.sourceUrl())) continue;
            citations.add(new GraduateCitation("route-fee-" + row.feeId(), "S" + (i + 1),
                    row.sourceTitle(), row.sourceUrl(), "FEE", row.universityId(), row.universityName(),
                    null, row.programName()));
        }
        return List.copyOf(citations);
    }

    private String money(BigDecimal amount, String currency) {
        if (amount == null) return null;
        return (text(currency) ? currency + " " : "") + amount.toPlainString();
    }

    private void append(StringBuilder builder, String label, String value) {
        if (text(value)) builder.append(" | ").append(label).append(": ").append(value.replace('\n', ' '));
    }

    private boolean text(String value) { return value != null && !value.isBlank(); }

    private static final class Values {
        String programName;
        String degreeLevel;
        String facultyName;
        String departmentName;
        String academicYear;
        String currency;
        String billingBasis;
        String scopeLevel;
        Integer limit;

        static Values from(Object value) {
            Values v = new Values();
            if (value instanceof GraduateRouteArguments.TuitionArguments a) {
                v.programName=a.programName(); v.degreeLevel=name(a.degreeType()); v.facultyName=a.facultyName();
                v.departmentName=a.departmentName(); v.academicYear=a.academicYear(); v.currency=a.currency();
                v.billingBasis=name(a.billingBasis()); v.scopeLevel=name(a.scopeLevel()); v.limit=a.limit();
            } else if (value instanceof GraduateRouteArguments.ProgramTuitionArguments a) {
                v.programName=a.programName(); v.degreeLevel=name(a.degreeType()); v.academicYear=a.academicYear();
                v.currency=a.currency(); v.billingBasis=name(a.billingBasis()); v.limit=a.limit();
            } else if (value instanceof GraduateRouteArguments.FacultyTuitionArguments a) {
                v.facultyName=a.facultyName(); v.degreeLevel=name(a.degreeType()); v.academicYear=a.academicYear();
                v.currency=a.currency(); v.billingBasis=name(a.billingBasis()); v.limit=a.limit();
            } else if (value instanceof GraduateRouteArguments.DepartmentTuitionArguments a) {
                v.departmentName=a.departmentName(); v.degreeLevel=name(a.degreeType()); v.academicYear=a.academicYear();
                v.currency=a.currency(); v.billingBasis=name(a.billingBasis()); v.limit=a.limit();
            } else if (value instanceof GraduateRouteArguments.UniversityTuitionArguments a) {
                v.degreeLevel=name(a.degreeType()); v.academicYear=a.academicYear(); v.currency=a.currency();
                v.billingBasis=name(a.billingBasis()); v.limit=a.limit();
            } else if (value instanceof GraduateRouteArguments.TuitionAggregateArguments a) {
                v.programName=a.programName(); v.degreeLevel=name(a.degreeType()); v.academicYear=a.academicYear();
                v.currency=a.currency(); v.billingBasis=name(a.billingBasis()); v.scopeLevel=name(a.scopeLevel()); v.limit=100;
            } else if (value instanceof GraduateRouteArguments.CompareTuitionArguments a) {
                v.programName=a.programName(); v.degreeLevel=name(a.degreeType()); v.academicYear=a.academicYear();
                v.currency=a.currency(); v.billingBasis=name(a.billingBasis()); v.limit=100;
            } else if (value instanceof GraduateRouteArguments.FeeArguments a) {
                v.programName=a.programName(); v.facultyName=a.facultyName(); v.departmentName=a.departmentName();
                v.academicYear=a.academicYear(); v.currency=a.currency(); v.limit=a.limit();
            } else throw new GraduateRoutePlanningException("Unsupported tuition argument type: " + value.getClass().getSimpleName());
            return v;
        }

        private static String name(Enum<?> value) { return value == null ? null : value.name(); }
    }
}
