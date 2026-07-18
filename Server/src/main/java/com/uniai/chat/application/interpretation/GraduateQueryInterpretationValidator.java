package com.uniai.chat.application.interpretation;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeFilters;
import com.uniai.chat.application.retrieval.GraduateKnowledgeOperation;
import com.uniai.chat.application.retrieval.GraduateKnowledgeAggregation;
import com.uniai.chat.application.retrieval.GraduateKnowledgeAggregationFunction;
import com.uniai.chat.application.retrieval.GraduateKnowledgeSort;
import com.uniai.chat.application.retrieval.GraduateKnowledgeSortDirection;
import com.uniai.chat.application.retrieval.GraduateKnowledgeSortField;
import com.uniai.chat.application.retrieval.GraduateKnowledgeThresholdOperator;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeResource;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class GraduateQueryInterpretationValidator {

    private static final int MAX_UNIVERSITIES = 3;
    private static final int MAX_DEGREE_TYPES = 4;
    private static final int MAX_TOPIC_KEYWORDS = 5;
    private static final int MAX_UNSUPPORTED_CONSTRAINTS = 5;
    private static final int MAX_CITY_LENGTH = 120;
    private static final int MAX_STRING_LENGTH = 120;
    private static final Pattern WORD_SPLIT = Pattern.compile("[^\\p{L}\\p{N}+]+");
    private static final Set<String> GENERIC_UNIVERSITY_TOKENS = Set.of(
            "university",
            "college",
            "institute",
            "school",
            "faculty",
            "department"
    );
    private static final Set<String> SUPPORTED_ADMISSION_TYPES = Set.of(
            "GENERAL", "GRE", "GMAT", "ENGLISH", "PORTFOLIO", "INTERVIEW",
            "EXPERIENCE", "ACADEMIC", "PREREQUISITE", "OTHER"
    );
    private static final Set<String> SUPPORTED_BILLING_BASES = Set.of(
            "PER_CREDIT", "PER_SEMESTER", "PER_YEAR", "PER_TERM", "PER_PROGRAM",
            "FLAT_FEE", "PER_APPLICATION", "PER_ACADEMIC_YEAR"
    );
    private static final Set<String> SUPPORTED_TUITION_SCOPES = Set.of("UNIVERSITY", "FACULTY", "DEPARTMENT", "PROGRAM");

    public GraduateQueryInterpretationResult validate(
            GraduateQueryInterpretation interpretation,
            List<UniversityCatalog> catalogs
    ) {
        if (interpretation == null) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_EMPTY");
        }

        if (interpretation.schemaVersion() == null || interpretation.schemaVersion() != 1) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_SCHEMA_VERSION_UNSUPPORTED");
        }

        GraduateKnowledgeIntent intent = normalizeIntent(interpretation.intent());
        if (intent == null) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_INVALID_INTENT");
        }

        if (tooLong(interpretation.intent())
                || tooLong(interpretation.detailLevel())
                || tooLong(interpretation.clarificationNeeded())
                || tooLong(interpretation.resource())
                || tooLong(interpretation.operation())
                || tooLong(interpretation.city())
                || tooLong(interpretation.faculty())
                || tooLong(interpretation.department())) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_VALUE_TOO_LONG");
        }

        List<String> normalizedUniversities = normalizeAndCapStrings(interpretation.universities(), MAX_UNIVERSITIES);
        List<String> normalizedDegrees = normalizeAndCapStrings(interpretation.degreeTypes(), MAX_DEGREE_TYPES);
        List<String> normalizedTopicKeywords = normalizeAndCapStrings(interpretation.topicKeywords(), MAX_TOPIC_KEYWORDS);
        List<String> normalizedUnsupportedConstraints = normalizeAndCapStrings(interpretation.unsupportedConstraints(), MAX_UNSUPPORTED_CONSTRAINTS);
        String normalizedCity = normalizeOptionalText(interpretation.city(), MAX_CITY_LENGTH);
        String normalizedFaculty = normalizeOptionalText(interpretation.faculty(), MAX_STRING_LENGTH);
        String normalizedDepartment = normalizeOptionalText(interpretation.department(), MAX_STRING_LENGTH);
        List<String> normalizedLanguages = normalizeAndCapStrings(interpretation.languages(), 4);
        List<String> normalizedAdmissionTypes = normalizeAndCapStrings(interpretation.admissionRequirementTypes(), 5);
        String normalizedProgramName = normalizeOptionalText(interpretation.programName(), MAX_STRING_LENGTH);

        if (normalizedUniversities == null
                || normalizedDegrees == null
                || normalizedTopicKeywords == null
                || normalizedUnsupportedConstraints == null
                || normalizedLanguages == null
                || normalizedAdmissionTypes == null) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_TOO_LARGE");
        }

        if (containsUnsupportedDegree(normalizedDegrees) || containsUnsupportedConstraint(normalizedUnsupportedConstraints)) {
            return GraduateQueryInterpretationResult.unsupported(
                    buildUnsupportedMessage(normalizedDegrees, normalizedUnsupportedConstraints),
                    0,
                    countSupportedDegrees(normalizedDegrees),
                    mergeUnsupportedConstraints(normalizedDegrees, normalizedUnsupportedConstraints)
            );
        }

        if (normalizedAdmissionTypes.stream()
                .map(value -> value.toUpperCase(Locale.ROOT))
                .anyMatch(value -> !SUPPORTED_ADMISSION_TYPES.contains(value))) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_ADMISSION_TYPE_UNSUPPORTED");
        }

        List<ResolvedUniversity> resolvedUniversities = resolveUniversities(normalizedUniversities, catalogs);
        GraduateKnowledgeQuery partialQuery;
        try {
            partialQuery = buildQuery(
                    intent,
                    resolvedUniversities,
                    normalizedDegrees,
                    normalizedTopicKeywords,
                    interpretation,
                    normalizedCity,
                    normalizedFaculty,
                    normalizedDepartment,
                    normalizedLanguages,
                    normalizedAdmissionTypes,
                    normalizedProgramName
            );
        } catch (IllegalArgumentException ex) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_RESOURCE_OPERATION_UNSUPPORTED");
        }
        if (requiresUniversity(intent) && resolvedUniversities.isEmpty()
                && (partialQuery == null || partialQuery.filters().city() == null)) {
            return GraduateQueryInterpretationResult.ambiguous(
                    buildAmbiguousMessage(intent),
                    0,
                    countSupportedDegrees(normalizedDegrees),
                    partialQuery
            );
        }

        if (intent == GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS) {
            return GraduateQueryInterpretationResult.ambiguous(
                    buildAmbiguousMessage(intent),
                    resolvedUniversities.size(),
                    countSupportedDegrees(normalizedDegrees),
                    partialQuery
            );
        }

        return GraduateQueryInterpretationResult.valid(partialQuery, resolvedUniversities.size(), countSupportedDegrees(normalizedDegrees));
    }

    private GraduateKnowledgeQuery buildQuery(
            GraduateKnowledgeIntent intent,
            List<ResolvedUniversity> resolvedUniversities,
            List<String> degreeTypes,
            List<String> topicKeywords,
            GraduateQueryInterpretation interpretation,
            String city,
            String faculty,
            String department,
            List<String> languages,
            List<String> admissionRequirementTypes,
            String programName
    ) {
        GraduateProgramDetailLevel detailLevel = normalizeDetailLevel(interpretation.detailLevel());
        boolean followUpResolved = Boolean.TRUE.equals(interpretation.followUp()) || Boolean.TRUE.equals(interpretation.comparison());
        List<String> normalizedDegrees = normalizeSupportedDegrees(degreeTypes);
        GraduateKnowledgeAggregation aggregation = parseAggregation(intent, interpretation.aggregation());
        GraduateKnowledgeThresholdOperator thresholdOperator = parseThresholdOperator(interpretation.thresholdOperator());
        BigDecimal thresholdValue = parseThresholdValue(interpretation.thresholdValue(), thresholdOperator);
        String currency = normalizeTuitionDimension(interpretation.currency(), "currency");
        String billingBasis = normalizeTuitionDimension(interpretation.billingBasis(), "billing basis");
        String academicYear = normalizeTuitionDimension(interpretation.academicYear(), "academic year");
        String tuitionScope = normalizeTuitionDimension(interpretation.tuitionScopeLevel(), "tuition scope");
        if (intent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                && aggregation.function() == GraduateKnowledgeAggregationFunction.RANGE
                && thresholdOperator != GraduateKnowledgeThresholdOperator.NONE) {
            throw new IllegalArgumentException("RANGE cannot be combined with a tuition threshold");
        }
        boolean hasProgramIdentityFilter = !topicKeywords.isEmpty()
                || programName != null
                || !languages.isEmpty()
                || !admissionRequirementTypes.isEmpty();
        if (intent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                && hasProgramIdentityFilter
                && tuitionScope != null
                && !"PROGRAM".equals(tuitionScope)) {
            throw new IllegalArgumentException("Program filters require PROGRAM tuition scope");
        }
        if (billingBasis != null && !SUPPORTED_BILLING_BASES.contains(billingBasis)) {
            throw new IllegalArgumentException("Unsupported billing basis");
        }
        if (tuitionScope != null && !SUPPORTED_TUITION_SCOPES.contains(tuitionScope)) {
            throw new IllegalArgumentException("Unsupported tuition scope");
        }
        GraduateKnowledgeFilters filters = new GraduateKnowledgeFilters(
                resolvedUniversities, normalizedDegrees, topicKeywords, city, faculty, department,
                languages, admissionRequirementTypes, programName, currency,
                billingBasis, academicYear, tuitionScope,
                thresholdOperator, thresholdValue
        );
        GraduateKnowledgeSort sort = parseSort(interpretation.sortField(), interpretation.sortDirection());
        Integer limit = parseLimit(interpretation.limit());
        GraduateProgramDetailLevel queryDetailLevel = intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP ? detailLevel : null;
        String resourceValue = normalizeOptionalValue(interpretation.resource());
        String operationValue = normalizeOptionalValue(interpretation.operation());
        if (resourceValue == null && operationValue == null) {
            if (intent == GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP) {
                throw new IllegalArgumentException("Academic structure routing metadata is required");
            }
            return new GraduateKnowledgeQuery(
                    intent,
                    GraduateKnowledgeQuery.resourceFor(intent),
                    GraduateKnowledgeQuery.operationFor(intent, queryDetailLevel),
                    filters,
                    aggregation,
                    sort,
                    limit,
                    com.uniai.chat.application.retrieval.GraduateKnowledgeFollowUpContext.empty(),
                    queryDetailLevel,
                    followUpResolved,
                    false
            );
        }
        if (resourceValue == null || operationValue == null) {
            throw new IllegalArgumentException("Both resource and operation are required when typed routing metadata is supplied");
        }

        GraduateKnowledgeResource resource = GraduateKnowledgeResource.valueOf(resourceValue);
        GraduateKnowledgeOperation operation = GraduateKnowledgeOperation.valueOf(operationValue);
        if (!GraduateKnowledgeQuery.isCompatible(resource, operation) || !matchesIntent(intent, resource, operation)) {
            throw new IllegalArgumentException("Unsupported graduate resource/operation combination");
        }
        return new GraduateKnowledgeQuery(
                intent,
                resource,
                operation,
                filters,
                aggregation,
                sort,
                limit,
                com.uniai.chat.application.retrieval.GraduateKnowledgeFollowUpContext.empty(),
                queryDetailLevel,
                followUpResolved,
                false
        );
    }

    private GraduateKnowledgeAggregation parseAggregation(GraduateKnowledgeIntent intent, String value) {
        if (intent != GraduateKnowledgeIntent.TUITION_AGGREGATION) {
            return GraduateKnowledgeAggregation.empty();
        }
        if (value == null || value.isBlank()) {
            return new GraduateKnowledgeAggregation(GraduateKnowledgeAggregationFunction.AVG, "tuition");
        }
        try {
            GraduateKnowledgeAggregationFunction function = GraduateKnowledgeAggregationFunction.valueOf(value.trim().toUpperCase(Locale.ROOT));
            if (function == GraduateKnowledgeAggregationFunction.AVG
                    || function == GraduateKnowledgeAggregationFunction.MIN
                    || function == GraduateKnowledgeAggregationFunction.MAX
                    || function == GraduateKnowledgeAggregationFunction.RANGE) {
                return new GraduateKnowledgeAggregation(function, "tuition");
            }
        } catch (IllegalArgumentException ignored) {
            // Report all unsupported values through the normal interpretation validation path.
        }
        throw new IllegalArgumentException("Unsupported tuition aggregation");
    }

    private GraduateKnowledgeThresholdOperator parseThresholdOperator(String value) {
        if (value == null || value.isBlank()) return GraduateKnowledgeThresholdOperator.NONE;
        try {
            GraduateKnowledgeThresholdOperator operator = GraduateKnowledgeThresholdOperator.valueOf(value.trim().toUpperCase(Locale.ROOT));
            if (operator != GraduateKnowledgeThresholdOperator.NONE) return operator;
        } catch (IllegalArgumentException ignored) {
            // Report all unsupported values through the normal interpretation validation path.
        }
        throw new IllegalArgumentException("Unsupported tuition threshold operator");
    }

    private BigDecimal parseThresholdValue(String value, GraduateKnowledgeThresholdOperator operator) {
        if (operator == GraduateKnowledgeThresholdOperator.NONE) {
            if (value != null && !value.isBlank()) throw new IllegalArgumentException("Threshold value requires an operator");
            return null;
        }
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Threshold value is required");
        try {
            BigDecimal parsed = new BigDecimal(value.trim());
            if (parsed.signum() < 0) throw new IllegalArgumentException("Threshold must not be negative");
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid tuition threshold");
        }
    }

    private GraduateKnowledgeSort parseSort(String field, String direction) {
        if (field == null || field.isBlank()) return GraduateKnowledgeSort.empty();
        try {
            GraduateKnowledgeSortField parsedField = GraduateKnowledgeSortField.valueOf(field.trim().toUpperCase(Locale.ROOT));
            GraduateKnowledgeSortDirection parsedDirection = direction == null || direction.isBlank()
                    ? GraduateKnowledgeSortDirection.ASC
                    : GraduateKnowledgeSortDirection.valueOf(direction.trim().toUpperCase(Locale.ROOT));
            if (parsedField == GraduateKnowledgeSortField.TUITION
                    || parsedField == GraduateKnowledgeSortField.NAME
                    || parsedField == GraduateKnowledgeSortField.UNIVERSITY
                    || parsedField == GraduateKnowledgeSortField.DEGREE_TYPE) {
                return new GraduateKnowledgeSort(parsedField, parsedDirection);
            }
        } catch (IllegalArgumentException ignored) {
            // Report all unsupported values through the normal interpretation validation path.
        }
        throw new IllegalArgumentException("Unsupported tuition sort");
    }

    private Integer parseLimit(Integer limit) {
        if (limit == null) return null;
        if (limit < 1 || limit > GraduateKnowledgeQuery.MAX_LIMIT) {
            throw new IllegalArgumentException("Query limit is outside the supported range");
        }
        return limit;
    }

    private String normalizeTuitionDimension(String value, String name) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > MAX_STRING_LENGTH || normalized.contains("\n") || normalized.contains("\r")) {
            throw new IllegalArgumentException("Invalid " + name);
        }
        if ("currency".equals(name) && !normalized.matches("[A-Z0-9][A-Z0-9_-]{0,9}")) {
            throw new IllegalArgumentException("Invalid currency");
        }
        return normalized;
    }

    private boolean matchesIntent(
            GraduateKnowledgeIntent intent,
            GraduateKnowledgeResource resource,
            GraduateKnowledgeOperation operation
    ) {
        return switch (intent) {
            case GENERAL_CHAT, UNKNOWN_OR_AMBIGUOUS -> resource == GraduateKnowledgeResource.NONE
                    && operation == GraduateKnowledgeOperation.NONE;
            case PROGRAM_LOOKUP -> resource == GraduateKnowledgeResource.PROGRAM
                    && (operation == GraduateKnowledgeOperation.LIST || operation == GraduateKnowledgeOperation.DETAILS);
            case TUITION_AGGREGATION -> resource == GraduateKnowledgeResource.PROGRAM
                    && operation == GraduateKnowledgeOperation.AGGREGATE;
            case GRADUATE_OVERVIEW -> resource == GraduateKnowledgeResource.GRADUATE_OVERVIEW
                    && operation == GraduateKnowledgeOperation.OVERVIEW;
            case LOCATION_LOOKUP -> (resource == GraduateKnowledgeResource.CAMPUS
                    || resource == GraduateKnowledgeResource.UNIVERSITY)
                    && (operation == GraduateKnowledgeOperation.LIST
                    || operation == GraduateKnowledgeOperation.COUNT
                    || operation == GraduateKnowledgeOperation.EXISTS);
            case ACADEMIC_STRUCTURE_LOOKUP -> (resource == GraduateKnowledgeResource.PROGRAM
                    || resource == GraduateKnowledgeResource.FACULTY
                    || resource == GraduateKnowledgeResource.DEPARTMENT)
                    && (operation == GraduateKnowledgeOperation.LIST
                    || operation == GraduateKnowledgeOperation.COUNT
                    || operation == GraduateKnowledgeOperation.EXISTS);
        };
    }

    private boolean requiresUniversity(GraduateKnowledgeIntent intent) {
        return intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP
                || intent == GraduateKnowledgeIntent.TUITION_AGGREGATION
                || intent == GraduateKnowledgeIntent.GRADUATE_OVERVIEW
                || intent == GraduateKnowledgeIntent.ACADEMIC_STRUCTURE_LOOKUP;
    }

    private GraduateKnowledgeIntent normalizeIntent(String intent) {
        if (intent == null || intent.isBlank()) {
            return null;
        }

        try {
            return GraduateKnowledgeIntent.valueOf(intent.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String normalizeOptionalValue(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalText(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        return normalized.length() > maxLength ? null : normalized;
    }

    private GraduateProgramDetailLevel normalizeDetailLevel(String detailLevel) {
        if (detailLevel == null || detailLevel.isBlank()) {
            return GraduateProgramDetailLevel.LIST;
        }
        try {
            return GraduateProgramDetailLevel.valueOf(detailLevel.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return GraduateProgramDetailLevel.LIST;
        }
    }

    private List<ResolvedUniversity> resolveUniversities(List<String> universityMentions, List<UniversityCatalog> catalogs) {
        if (universityMentions == null || universityMentions.isEmpty() || catalogs == null || catalogs.isEmpty()) {
            return List.of();
        }

        Map<Long, ResolvedUniversity> resolved = new LinkedHashMap<>();
        for (String mention : universityMentions) {
            if (mention == null || mention.isBlank()) {
                continue;
            }
            for (UniversityCatalog catalog : catalogs) {
                if (catalog == null || catalog.getId() == null) {
                    continue;
                }
                if (matchesUniversity(mention, catalog)) {
                    resolved.putIfAbsent(catalog.getId(), new ResolvedUniversity(catalog.getId(), catalog.getName(), catalog.getAcronym()));
                }
            }
        }
        return new ArrayList<>(resolved.values()).stream().limit(MAX_UNIVERSITIES).toList();
    }

    private boolean matchesUniversity(String mention, UniversityCatalog university) {
        if (mention == null || mention.isBlank() || university == null) {
            return false;
        }

        String normalizedMention = normalizeText(mention);

        if (hasText(university.getAcronym()) && containsWord(normalizedMention, normalizeText(university.getAcronym()))) {
            return true;
        }

        if (hasText(university.getName()) && normalizedMention.contains(normalizeText(university.getName()))) {
            return true;
        }

        if (hasText(university.getNameAr()) && normalizedMention.contains(normalizeText(university.getNameAr()))) {
            return true;
        }

        if (hasText(university.getName())) {
            for (String token : tokenize(university.getName())) {
                if (token.length() > 2 && !GENERIC_UNIVERSITY_TOKENS.contains(token) && containsWord(normalizedMention, token)) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<String> normalizeSupportedDegrees(List<String> degreeTypes) {
        if (degreeTypes == null || degreeTypes.isEmpty()) {
            return List.of();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String degreeType : degreeTypes) {
            if (!hasText(degreeType)) {
                continue;
            }
            String value = degreeType.trim().toUpperCase(Locale.ROOT);
            if ("MASTER".equals(value) || "PHD".equals(value)) {
                normalized.add(value);
            }
        }
        return new ArrayList<>(normalized);
    }

    private boolean containsUnsupportedDegree(List<String> degreeTypes) {
        if (degreeTypes == null || degreeTypes.isEmpty()) {
            return false;
        }
        for (String degreeType : degreeTypes) {
            if (!hasText(degreeType)) {
                continue;
            }
            String value = degreeType.trim().toUpperCase(Locale.ROOT);
            if ("BACHELOR".equals(value) || "UNDERGRADUATE".equals(value) || "UNDERGRAD".equals(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsUnsupportedConstraint(List<String> unsupportedConstraints) {
        if (unsupportedConstraints == null || unsupportedConstraints.isEmpty()) {
            return false;
        }
        for (String value : unsupportedConstraints) {
            if (!hasText(value)) {
                continue;
            }
            String normalized = value.trim().toUpperCase(Locale.ROOT);
            if (normalized.contains("BACHELOR") || normalized.contains("UNDERGRADUATE") || normalized.contains("UNDERGRAD")) {
                return true;
            }
        }
        return false;
    }

    private List<String> mergeUnsupportedConstraints(List<String> degreeTypes, List<String> unsupportedConstraints) {
        Set<String> values = new LinkedHashSet<>();
        if (degreeTypes != null) {
            for (String value : degreeTypes) {
                if (hasText(value)) {
                    values.add(value.trim().toUpperCase(Locale.ROOT));
                }
            }
        }
        if (unsupportedConstraints != null) {
            for (String value : unsupportedConstraints) {
                if (hasText(value)) {
                    values.add(value.trim().toUpperCase(Locale.ROOT));
                }
            }
        }
        return values.stream().limit(MAX_UNSUPPORTED_CONSTRAINTS).toList();
    }

    private int countSupportedDegrees(List<String> degreeTypes) {
        return degreeTypes == null ? 0 : degreeTypes.size();
    }

    private String buildUnsupportedMessage(List<String> degreeTypes, List<String> unsupportedConstraints) {
        return "I can help with master's and PhD graduate questions only.";
    }

    private String buildAmbiguousMessage(GraduateKnowledgeIntent intent) {
        if (intent == GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS) {
            return "I need a clearer university or graduate query to answer safely.";
        }
        return "I need a clearer university reference to answer safely.";
    }

    private List<String> normalizeAndCapStrings(List<String> values, int maxSize) {
        if (values == null) {
            return List.of();
        }
        if (values.size() > maxSize) {
            return null;
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (!hasText(value)) {
                continue;
            }
            String trimmed = value.trim();
            if (trimmed.length() > MAX_STRING_LENGTH) {
                return null;
            }
            normalized.add(trimmed);
        }
        return new ArrayList<>(normalized);
    }

    private boolean tooLong(String value) {
        return value != null && value.length() > MAX_STRING_LENGTH;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean containsWord(String haystack, String needle) {
        if (!hasText(haystack) || !hasText(needle)) {
            return false;
        }
        String[] tokens = WORD_SPLIT.split(haystack);
        for (String token : tokens) {
            if (token.equalsIgnoreCase(needle)) {
                return true;
            }
        }
        return false;
    }

    private List<String> tokenize(String value) {
        if (!hasText(value)) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        for (String token : WORD_SPLIT.split(normalizeText(value))) {
            if (hasText(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }
}
