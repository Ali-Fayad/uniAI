package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Validates business-safe argument invariants after structural JSON parsing. */
public final class GraduateRouteArgumentValidator {
    private static final int MAX_TEXT_LENGTH = 160;
    private static final int MAX_COMPARISON_UNIVERSITIES = 10;
    private static final Pattern ACADEMIC_YEAR = Pattern.compile("\\d{4}-\\d{4}");
    private static final Pattern CURRENCY = Pattern.compile("[A-Za-z]{3}");

    public void validate(ValidatedGraduateRoutePlan<?> plan) {
        if (plan == null || plan.route() == null || plan.arguments() == null || plan.canonicalArguments() == null) {
            throw invalid("PLAN_INCOMPLETE");
        }
        validateNode(plan.canonicalArguments());
        validateAcademicYear(plan.canonicalArguments().get("academicYear"));
        validateCurrency(plan.canonicalArguments().get("currency"));
        validateCollectionSizes(plan.canonicalArguments());
        validateUniversityList(plan);
        validateRankingLimit(plan);
        validateRouteInvariant(plan);
    }

    private void validateNode(JsonNode node) {
        Iterator<JsonNode> values = node.elements();
        while (values.hasNext()) {
            JsonNode value = values.next();
            if (value.isTextual()) {
                String text = value.textValue();
                if (text.isBlank() || text.length() > MAX_TEXT_LENGTH || text.contains("\n") || text.contains("\r")) {
                    throw invalid("TEXT_ARGUMENT_INVALID");
                }
            } else if (value.isArray()) {
                Set<String> unique = new HashSet<>();
                for (JsonNode element : value) {
                    if (element.isTextual() && !unique.add(element.textValue().trim().toLowerCase(Locale.ROOT))) {
                        throw invalid("DUPLICATE_LIST_ARGUMENT");
                    }
                }
            }
        }
    }

    private void validateAcademicYear(JsonNode value) {
        if (value != null && !value.isNull() && !ACADEMIC_YEAR.matcher(value.textValue()).matches()) {
            throw invalid("ACADEMIC_YEAR_INVALID");
        }
    }

    private void validateCurrency(JsonNode value) {
        if (value != null && !value.isNull() && !CURRENCY.matcher(value.textValue()).matches()) {
            throw invalid("CURRENCY_INVALID");
        }
    }

    private void validateUniversityList(ValidatedGraduateRoutePlan<?> plan) {
        JsonNode value = plan.canonicalArguments().get("universities");
        if (value != null && !value.isNull()
                && isComparison(plan.route())
                && (value.size() < 1 || value.size() > MAX_COMPARISON_UNIVERSITIES)) {
            throw invalid("UNIVERSITY_LIST_SIZE_INVALID");
        }
    }

    private void validateCollectionSizes(JsonNode args) {
        validateListSize(args, "universities", 20);
        validateListSize(args, "faculties", 30);
        validateListSize(args, "departments", 50);
        validateListSize(args, "programs", 50);
        validateListSize(args, "degreeTypes", 20);
        validateListSize(args, "cities", 30);
    }

    private void validateListSize(JsonNode args, String field, int maximum) {
        JsonNode value = args.get(field);
        if (value != null && !value.isNull() && (!value.isArray() || value.size() > maximum)) {
            throw invalid(field.toUpperCase(Locale.ROOT) + "_LIST_SIZE_INVALID");
        }
    }

    private void validateRankingLimit(ValidatedGraduateRoutePlan<?> plan) {
        if (plan.route() != GraduateAiRoute.RANK_UNIVERSITIES_BY_TUITION
                && plan.route() != GraduateAiRoute.RANK_PROGRAMS_BY_TUITION) return;
        JsonNode limit = plan.canonicalArguments().get("limit");
        if (limit != null && !limit.isNull() && (!limit.isIntegralNumber() || limit.asInt() < 1 || limit.asInt() > 10)) {
            throw invalid("RANKING_LIMIT_INVALID");
        }
    }

    private boolean isComparison(GraduateAiRoute route) {
        return switch (route) {
            case COMPARE_UNIVERSITIES, COMPARE_CAMPUS_COUNTS, COMPARE_PROGRAM_AVAILABILITY,
                    COMPARE_PROGRAM_COUNTS, COMPARE_TUITION, COMPARE_ADMISSION_REQUIREMENTS -> true;
            default -> false;
        };
    }

    private void validateRouteInvariant(ValidatedGraduateRoutePlan<?> plan) {
        JsonNode args = plan.canonicalArguments();
        switch (plan.route()) {
            case COMPARE_UNIVERSITIES, COMPARE_CAMPUS_COUNTS, COMPARE_PROGRAM_AVAILABILITY,
                    COMPARE_PROGRAM_COUNTS, COMPARE_TUITION, COMPARE_ADMISSION_REQUIREMENTS -> {
                JsonNode universities = args.get("universities");
                if (universities == null || !universities.isArray() || universities.size() < 2) {
                    throw invalid("COMPARISON_REQUIRES_TWO_UNIVERSITIES");
                }
            }
            case CHECK_CAMPUS_EXISTS -> requireAny(args, "campusName", "university", "city");
            default -> {
                // Structural and enum rules are route-specific through each argument record.
            }
        }
    }

    private void requireAny(JsonNode args, String... fields) {
        for (String field : fields) {
            JsonNode value = args.get(field);
            if (value != null && !value.isNull() && (!value.isTextual() || !value.textValue().isBlank())) return;
        }
        throw invalid("AT_LEAST_ONE_SCOPE_ARGUMENT_REQUIRED");
    }

    private GraduateRoutePlanningException invalid(String reason) {
        return new GraduateRoutePlanningException("Invalid graduate route arguments: " + reason);
    }
}
