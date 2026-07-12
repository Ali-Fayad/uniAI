package com.uniai.chat.application.interpretation;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateProgramDetailLevel;
import com.uniai.chat.application.retrieval.ResolvedUniversity;

import java.util.ArrayList;
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
                || tooLong(interpretation.clarificationNeeded())) {
            return GraduateQueryInterpretationResult.invalid("AI_QUERY_INTERPRETATION_VALUE_TOO_LONG");
        }

        List<String> normalizedUniversities = normalizeAndCapStrings(interpretation.universities(), MAX_UNIVERSITIES);
        List<String> normalizedDegrees = normalizeAndCapStrings(interpretation.degreeTypes(), MAX_DEGREE_TYPES);
        List<String> normalizedTopicKeywords = normalizeAndCapStrings(interpretation.topicKeywords(), MAX_TOPIC_KEYWORDS);
        List<String> normalizedUnsupportedConstraints = normalizeAndCapStrings(interpretation.unsupportedConstraints(), MAX_UNSUPPORTED_CONSTRAINTS);

        if (normalizedUniversities == null
                || normalizedDegrees == null
                || normalizedTopicKeywords == null
                || normalizedUnsupportedConstraints == null) {
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

        List<ResolvedUniversity> resolvedUniversities = resolveUniversities(normalizedUniversities, catalogs);
        GraduateKnowledgeQuery partialQuery = buildQuery(
                intent,
                resolvedUniversities,
                normalizedDegrees,
                interpretation
        );
        if (requiresUniversity(intent) && resolvedUniversities.isEmpty()) {
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
            GraduateQueryInterpretation interpretation
    ) {
        GraduateProgramDetailLevel detailLevel = normalizeDetailLevel(interpretation.detailLevel());
        boolean followUpResolved = Boolean.TRUE.equals(interpretation.followUp()) || Boolean.TRUE.equals(interpretation.comparison());
        List<String> normalizedDegrees = normalizeSupportedDegrees(degreeTypes);
        return new GraduateKnowledgeQuery(
                intent,
                resolvedUniversities,
                normalizedDegrees,
                intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP ? detailLevel : null,
                followUpResolved,
                false
        );
    }

    private boolean requiresUniversity(GraduateKnowledgeIntent intent) {
        return intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP || intent == GraduateKnowledgeIntent.TUITION_AGGREGATION;
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
