package com.uniai.chat.application.memory;

import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.chat.application.retrieval.GraduateKnowledgeEntityResolutionResult;
import com.uniai.chat.application.retrieval.GraduateKnowledgeEntityResolutionStatus;
import com.uniai.chat.application.retrieval.GraduateKnowledgeEntityResolver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ConversationMemoryValidator {

    private final GraduateKnowledgeEntityResolver universityResolver = new GraduateKnowledgeEntityResolver();

    private static final int MAX_UNIVERSITIES = 3;
    private static final int MAX_DEGREE_TYPES = 4;
    private static final int MAX_PENDING_TOPICS = 5;
    private static final int MAX_CORRECTIONS = 5;
    private static final int MAX_CLEAR_FIELDS = 8;
    private static final int MAX_STRING_LENGTH = 120;

    private static final Set<String> ALLOWED_INTENTS = Set.of(
            "PROGRAM_LOOKUP",
            "TUITION_AGGREGATION",
            "GRADUATE_OVERVIEW",
            "UNKNOWN_OR_AMBIGUOUS"
    );
    private static final Set<String> ALLOWED_DEGREE_TYPES = Set.of("MASTER", "PHD");
    private static final Set<String> ALLOWED_UNSUPPORTED_DEGREES = Set.of("BACHELOR", "UNDERGRADUATE", "UNDERGRAD");
    private static final Set<String> ALLOWED_CLEAR_FIELDS = Set.of(
            "activeUniversities",
            "activeDegreeTypes",
            "comparisonUniversities",
            "pendingTopics",
            "corrections",
            "unresolvedReferences",
            "userPreferences",
            "comparisonActive",
            "lastIntent"
    );
    private static final Set<String> ALLOWED_PREFERENCE_LANGUAGES = Set.of("ENGLISH", "ARABIC", "BOTH");
    private static final Set<String> ALLOWED_PREFERENCE_AFFORDABILITY = Set.of("LOWER_TUITION_FIRST", "BALANCED", "NO_PREFERENCE");
    private static final Set<String> ALLOWED_PREFERENCE_DELIVERY = Set.of("ONLINE", "ON_CAMPUS", "HYBRID", "ANY");

    public ValidationResult validatePatch(ConversationMemoryPatch patch, List<UniversityCatalog> catalogs) {
        if (patch == null || patch.schemaVersion() == null || patch.schemaVersion() != ConversationMemory.SCHEMA_VERSION) {
            return ValidationResult.invalid("AI_MEMORY_SCHEMA_VERSION_UNSUPPORTED");
        }

        if (tooLong(patch.setLastIntent())
                || tooLong(patch.setAllowedPreferences() != null ? patch.setAllowedPreferences().preferredLanguage() : null)
                || tooLong(patch.setAllowedPreferences() != null ? patch.setAllowedPreferences().affordabilityPriority() : null)
                || tooLong(patch.setAllowedPreferences() != null ? patch.setAllowedPreferences().preferredDeliveryMode() : null)) {
            return ValidationResult.invalid("AI_MEMORY_VALUE_TOO_LONG");
        }

        if (containsUnsupportedDegree(patch.replaceActiveDegreeTypes())
                || containsUnsupportedDegree(patch.addActiveDegreeTypes())
                || containsUnsupportedDegree(patch.removeActiveDegreeTypes())) {
            return ValidationResult.unsupported("AI_MEMORY_UNSUPPORTED_DEGREE");
        }

        List<MemoryUniversityRef> replaceUniversities = resolveUniversities(patch.replaceActiveUniversities(), catalogs, MAX_UNIVERSITIES);
        List<MemoryUniversityRef> addUniversities = resolveUniversities(patch.addActiveUniversities(), catalogs, MAX_UNIVERSITIES);
        List<MemoryUniversityRef> removeUniversities = resolveUniversities(patch.removeActiveUniversities(), catalogs, MAX_UNIVERSITIES);
        List<MemoryUniversityRef> replaceComparisonUniversities = resolveUniversities(patch.replaceComparisonUniversities(), catalogs, MAX_UNIVERSITIES);
        if (replaceUniversities == null || addUniversities == null || removeUniversities == null || replaceComparisonUniversities == null) {
            return ValidationResult.invalid("AI_MEMORY_INVALID_UNIVERSITY");
        }

        List<String> replaceDegrees = normalizeDegrees(patch.replaceActiveDegreeTypes(), MAX_DEGREE_TYPES);
        List<String> addDegrees = normalizeDegrees(patch.addActiveDegreeTypes(), MAX_DEGREE_TYPES);
        List<String> removeDegrees = normalizeDegrees(patch.removeActiveDegreeTypes(), MAX_DEGREE_TYPES);
        if (replaceDegrees == null || addDegrees == null || removeDegrees == null) {
            return ValidationResult.invalid("AI_MEMORY_TOO_LARGE");
        }

        List<String> pendingTopics = normalizeStrings(patch.addPendingTopics(), MAX_PENDING_TOPICS);
        List<String> corrections = normalizeStrings(patch.addCorrections(), MAX_CORRECTIONS);
        List<String> clearFields = normalizeStrings(patch.clearFields(), MAX_CLEAR_FIELDS);
        if (pendingTopics == null || corrections == null || clearFields == null) {
            return ValidationResult.invalid("AI_MEMORY_TOO_LARGE");
        }

        if (!clearFields.isEmpty() && clearFields.stream().anyMatch(value -> !ALLOWED_CLEAR_FIELDS.contains(value))) {
            return ValidationResult.invalid("AI_MEMORY_INVALID_CLEAR_FIELD");
        }

        if (patch.setLastIntent() != null && !ALLOWED_INTENTS.contains(patch.setLastIntent().trim().toUpperCase(Locale.ROOT))) {
            return ValidationResult.invalid("AI_MEMORY_INVALID_INTENT");
        }

        ConversationPreferences preferences = patch.setAllowedPreferences();
        if (preferences != null && !preferencesAreAllowlisted(preferences)) {
            return ValidationResult.invalid("AI_MEMORY_INVALID_PREFERENCE");
        }

        return ValidationResult.valid();
    }

    private boolean containsUnsupportedDegree(List<String> degreeTypes) {
        if (degreeTypes == null) {
            return false;
        }
        for (String degreeType : degreeTypes) {
            if (degreeType == null) {
                continue;
            }
            String normalized = degreeType.trim().toUpperCase(Locale.ROOT);
            if (ALLOWED_UNSUPPORTED_DEGREES.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private boolean preferencesAreAllowlisted(ConversationPreferences preferences) {
        return isAllowedPreferenceValue(preferences.preferredLanguage(), ALLOWED_PREFERENCE_LANGUAGES)
                && isAllowedPreferenceValue(preferences.affordabilityPriority(), ALLOWED_PREFERENCE_AFFORDABILITY)
                && isAllowedPreferenceValue(preferences.preferredDeliveryMode(), ALLOWED_PREFERENCE_DELIVERY);
    }

    private boolean isAllowedPreferenceValue(String value, Set<String> allowlist) {
        if (value == null) {
            return true;
        }
        String normalized = normalizeCode(value);
        return normalized != null && allowlist.contains(normalized);
    }

    private boolean tooLong(String value) {
        return value != null && value.length() > MAX_STRING_LENGTH;
    }

    private List<MemoryUniversityRef> resolveUniversities(List<String> mentions, List<UniversityCatalog> catalogs, int limit) {
        if (mentions == null || mentions.isEmpty()) {
            return List.of();
        }
        if (catalogs == null) {
            catalogs = List.of();
        }
        Map<Long, MemoryUniversityRef> resolved = new LinkedHashMap<>();
        for (String mention : mentions) {
            if (mention == null || mention.isBlank()) {
                continue;
            }
            GraduateKnowledgeEntityResolutionResult resolution = universityResolver.resolve(List.of(mention), catalogs, mention);
            if (resolution.status() != GraduateKnowledgeEntityResolutionStatus.RESOLVED) {
                return null;
            }
            for (com.uniai.chat.application.retrieval.ResolvedUniversity university : resolution.universities()) {
                if (university != null && university.id() != null) {
                    resolved.putIfAbsent(university.id(), new MemoryUniversityRef(university.id(), university.name(), university.acronym()));
                }
            }
        }
        if (resolved.size() > limit) {
            return null;
        }
        return new ArrayList<>(resolved.values());
    }

    private List<String> normalizeDegrees(List<String> values, int limit) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            String upper = value.trim().toUpperCase(Locale.ROOT);
            if (ALLOWED_DEGREE_TYPES.contains(upper)) {
                normalized.add(upper);
            }
        }
        if (normalized.size() > limit) {
            return null;
        }
        return new ArrayList<>(normalized);
    }

    private List<String> normalizeStrings(List<String> values, int limit) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                normalized.add(value.trim());
            }
        }
        if (normalized.size() > limit) {
            return null;
        }
        return new ArrayList<>(normalized);
    }

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
        normalized = normalized.replaceAll("^_+", "").replaceAll("_+$", "");
        return normalized.isBlank() ? null : normalized;
    }

    public record ValidationResult(boolean accepted, boolean unsupported, String failureCategory) {
        public boolean isValid() {
            return accepted;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, false, "");
        }

        public static ValidationResult invalid(String category) {
            return new ValidationResult(false, false, category);
        }

        public static ValidationResult unsupported(String category) {
            return new ValidationResult(false, true, category);
        }
    }
}
