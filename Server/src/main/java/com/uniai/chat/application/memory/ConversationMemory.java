package com.uniai.chat.application.memory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uniai.chat.application.retrieval.GraduateKnowledgeIntent;

import java.util.ArrayList;
import java.util.List;

public record ConversationMemory(
        int schemaVersion,
        List<MemoryUniversityRef> activeUniversities,
        List<String> activeDegreeTypes,
        String lastIntent,
        boolean comparisonActive,
        List<MemoryUniversityRef> comparisonUniversities,
        List<String> pendingTopics,
        List<String> corrections,
        List<String> unresolvedReferences,
        ConversationPreferences userPreferences
) {
    public static final int SCHEMA_VERSION = 1;

    public ConversationMemory {
        activeUniversities = normalizeUniversities(activeUniversities);
        activeDegreeTypes = normalizeStrings(activeDegreeTypes);
        lastIntent = normalizeString(lastIntent);
        comparisonUniversities = normalizeUniversities(comparisonUniversities);
        pendingTopics = normalizeStrings(pendingTopics);
        corrections = normalizeStrings(corrections);
        unresolvedReferences = normalizeStrings(unresolvedReferences);
        userPreferences = userPreferences == null ? new ConversationPreferences(null, null, null) : userPreferences;
        schemaVersion = schemaVersion <= 0 ? SCHEMA_VERSION : schemaVersion;
    }

    public static ConversationMemory empty() {
        return new ConversationMemory(
                SCHEMA_VERSION,
                List.of(),
                List.of(),
                null,
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new ConversationPreferences(null, null, null)
        );
    }

    @JsonIgnore
    public boolean isEmpty() {
        return activeUniversities.isEmpty()
                && activeDegreeTypes.isEmpty()
                && !comparisonActive
                && comparisonUniversities.isEmpty()
                && pendingTopics.isEmpty()
                && corrections.isEmpty()
                && unresolvedReferences.isEmpty()
                && (userPreferences == null || userPreferences.isEmpty());
    }

    public boolean hasValidSchema() {
        return schemaVersion == SCHEMA_VERSION;
    }

    public String toPromptText() {
        return ConversationMemoryPromptFormatter.render(this);
    }

    public GraduateKnowledgeIntent lastIntentEnum() {
        if (lastIntent == null || lastIntent.isBlank()) {
            return GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS;
        }
        try {
            return GraduateKnowledgeIntent.valueOf(lastIntent.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS;
        }
    }

    private static List<MemoryUniversityRef> normalizeUniversities(List<MemoryUniversityRef> universities) {
        if (universities == null || universities.isEmpty()) {
            return List.of();
        }
        List<MemoryUniversityRef> normalized = new ArrayList<>();
        for (MemoryUniversityRef ref : universities) {
            if (ref == null || ref.id() == null) {
                continue;
            }
            normalized.add(ref);
        }
        return List.copyOf(normalized);
    }

    private static List<String> normalizeStrings(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            String clean = normalizeString(value);
            if (clean != null) {
                normalized.add(clean);
            }
        }
        return List.copyOf(normalized);
    }

    private static String normalizeString(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
