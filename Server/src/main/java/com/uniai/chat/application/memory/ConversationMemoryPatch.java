package com.uniai.chat.application.memory;

import java.util.ArrayList;
import java.util.List;

public record ConversationMemoryPatch(
        Integer schemaVersion,
        String setLastIntent,
        Boolean setComparisonActive,
        List<String> replaceActiveUniversities,
        List<String> addActiveUniversities,
        List<String> removeActiveUniversities,
        List<String> replaceActiveDegreeTypes,
        List<String> addActiveDegreeTypes,
        List<String> removeActiveDegreeTypes,
        List<String> replaceComparisonUniversities,
        List<String> addPendingTopics,
        List<String> removePendingTopics,
        List<String> addCorrections,
        List<String> removeCorrections,
        ConversationPreferences setAllowedPreferences,
        List<String> clearFields
) {
    public ConversationMemoryPatch {
        replaceActiveUniversities = normalize(replaceActiveUniversities);
        addActiveUniversities = normalize(addActiveUniversities);
        removeActiveUniversities = normalize(removeActiveUniversities);
        replaceActiveDegreeTypes = normalize(replaceActiveDegreeTypes);
        addActiveDegreeTypes = normalize(addActiveDegreeTypes);
        removeActiveDegreeTypes = normalize(removeActiveDegreeTypes);
        replaceComparisonUniversities = normalize(replaceComparisonUniversities);
        addPendingTopics = normalize(addPendingTopics);
        removePendingTopics = normalize(removePendingTopics);
        addCorrections = normalize(addCorrections);
        removeCorrections = normalize(removeCorrections);
        clearFields = normalize(clearFields);
        setLastIntent = normalize(setLastIntent);
        schemaVersion = schemaVersion == null || schemaVersion <= 0 ? ConversationMemory.SCHEMA_VERSION : schemaVersion;
    }

    public boolean isEmpty() {
        return setLastIntent == null
                && setComparisonActive == null
                && replaceActiveUniversities.isEmpty()
                && addActiveUniversities.isEmpty()
                && removeActiveUniversities.isEmpty()
                && replaceActiveDegreeTypes.isEmpty()
                && addActiveDegreeTypes.isEmpty()
                && removeActiveDegreeTypes.isEmpty()
                && replaceComparisonUniversities.isEmpty()
                && addPendingTopics.isEmpty()
                && removePendingTopics.isEmpty()
                && addCorrections.isEmpty()
                && removeCorrections.isEmpty()
                && setAllowedPreferences == null
                && clearFields.isEmpty();
    }

    private static List<String> normalize(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                normalized.add(value.trim());
            }
        }
        return List.copyOf(normalized);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
