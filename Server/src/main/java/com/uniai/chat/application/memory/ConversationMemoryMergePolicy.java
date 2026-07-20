package com.uniai.chat.application.memory;

import com.uniai.chat.application.planning.GraduateRouteExecutionResult;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import com.uniai.chat.application.retrieval.GraduateKnowledgeContextPolicy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ConversationMemoryMergePolicy {

    public ConversationMemory merge(ConversationMemory previous, ConversationMemoryPatch patch, GraduateRouteExecutionResult result) {
        return merge(previous, patch, result, GraduateKnowledgeContextPolicy.REFERENTIAL);
    }

    public ConversationMemory merge(ConversationMemory previous, ConversationMemoryPatch patch, GraduateRouteExecutionResult result,
                                    GraduateKnowledgeContextPolicy contextPolicy) {
        ConversationMemory base = previous == null ? ConversationMemory.empty() : previous;
        ConversationMemoryPatch effectivePatch = patch == null ? emptyPatch() : patch;

        List<MemoryUniversityRef> activeUniversities = base.activeUniversities();
        List<String> activeDegreeTypes = base.activeDegreeTypes();
        boolean comparisonActive = base.comparisonActive();
        List<MemoryUniversityRef> comparisonUniversities = base.comparisonUniversities();
        List<String> pendingTopics = base.pendingTopics();
        List<String> corrections = base.corrections();
        List<String> unresolvedReferences = base.unresolvedReferences();
        ConversationPreferences preferences = base.userPreferences();
        String lastIntent = base.lastIntent();

        if (effectivePatch.clearFields().contains("lastIntent")) {
            lastIntent = null;
        } else if (effectivePatch.setLastIntent() != null) {
            lastIntent = effectivePatch.setLastIntent().trim().toUpperCase(Locale.ROOT);
        } else if (result != null && result.route() != null) {
            lastIntent = result.route().name();
        }

        if (effectivePatch.clearFields().contains("comparisonActive")) {
            comparisonActive = false;
        } else if (effectivePatch.setComparisonActive() != null) {
            comparisonActive = effectivePatch.setComparisonActive();
        } else if (result != null) {
            comparisonActive = result.route().name().startsWith("COMPARE_") && result.resolvedUniversities().size() > 1;
        }

        boolean resetScope = contextPolicy == GraduateKnowledgeContextPolicy.STANDALONE
                || contextPolicy == GraduateKnowledgeContextPolicy.TOPIC_RESET;
        if (effectivePatch.clearFields().contains("activeUniversities") || resetScope && (result == null || result.resolvedUniversities().isEmpty())) {
            activeUniversities = List.of();
        } else if (!effectivePatch.replaceActiveUniversities().isEmpty()) {
            activeUniversities = resolveUniversities(effectivePatch.replaceActiveUniversities(), result);
        } else {
            List<MemoryUniversityRef> queryUniversities = result == null || result.resolvedUniversities() == null || result.resolvedUniversities().isEmpty()
                    ? activeUniversities
                    : resolvedUniversities(result.resolvedUniversities());
            activeUniversities = applyUniversityDelta(queryUniversities, effectivePatch.addActiveUniversities(), effectivePatch.removeActiveUniversities(), result);
        }

        if (effectivePatch.clearFields().contains("activeDegreeTypes") || resetScope && (result == null || extractDegrees(result).isEmpty())) {
            activeDegreeTypes = List.of();
        } else if (!effectivePatch.replaceActiveDegreeTypes().isEmpty()) {
            activeDegreeTypes = normalizeDegrees(effectivePatch.replaceActiveDegreeTypes());
        } else {
            List<String> queryDegrees = result == null || extractDegrees(result).isEmpty()
                    ? activeDegreeTypes
                    : extractDegrees(result);
            activeDegreeTypes = applyDegreeDelta(queryDegrees, effectivePatch.addActiveDegreeTypes(), effectivePatch.removeActiveDegreeTypes(), result);
        }

        if (effectivePatch.clearFields().contains("comparisonUniversities")) {
            comparisonUniversities = List.of();
        } else if (!effectivePatch.replaceComparisonUniversities().isEmpty()) {
            comparisonUniversities = resolveUniversities(effectivePatch.replaceComparisonUniversities(), result);
        } else if (comparisonActive) {
            comparisonUniversities = mergeComparisonUniversities(comparisonUniversities, result);
        } else {
            comparisonUniversities = List.of();
        }

        if (effectivePatch.clearFields().contains("pendingTopics")) {
            pendingTopics = List.of();
        } else {
            pendingTopics = applyStringDelta(pendingTopics, effectivePatch.addPendingTopics(), effectivePatch.removePendingTopics());
        }

        if (effectivePatch.clearFields().contains("corrections")) {
            corrections = List.of();
        } else {
            corrections = applyStringDelta(corrections, effectivePatch.addCorrections(), effectivePatch.removeCorrections());
        }

        if (effectivePatch.clearFields().contains("unresolvedReferences")) {
            unresolvedReferences = List.of();
        }

        if (effectivePatch.clearFields().contains("userPreferences")) {
            preferences = new ConversationPreferences(null, null, null);
        } else if (effectivePatch.setAllowedPreferences() != null) {
            preferences = effectivePatch.setAllowedPreferences();
        }

        List<com.uniai.chat.application.retrieval.GraduateKnowledgeReference> queryReferences = List.of();
        List<com.uniai.chat.application.retrieval.GraduateKnowledgeReference> activeReferences = base.activeReferences();
        List<com.uniai.chat.application.retrieval.GraduateKnowledgeReference> comparisonReferences = comparisonActive
                ? (queryReferences.isEmpty() ? base.comparisonReferences() : queryReferences)
                : List.of();
        return new ConversationMemory(
                ConversationMemory.SCHEMA_VERSION,
                activeUniversities,
                activeDegreeTypes,
                lastIntent,
                comparisonActive,
                comparisonUniversities,
                pendingTopics,
                corrections,
                unresolvedReferences,
                preferences,
                activeReferences,
                comparisonReferences,
                null
        );
    }

    private ConversationMemoryPatch emptyPatch() {
        return new ConversationMemoryPatch(
                ConversationMemory.SCHEMA_VERSION,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of()
        );
    }

    private List<MemoryUniversityRef> resolveUniversities(List<String> mentions, GraduateRouteExecutionResult result) {
        if (mentions == null || mentions.isEmpty()) {
            return List.of();
        }
        Map<Long, MemoryUniversityRef> byId = new LinkedHashMap<>();
        for (String mention : mentions) {
            if (mention == null || mention.isBlank()) {
                continue;
            }
            for (ResolvedUniversity university : result != null ? result.resolvedUniversities() : List.<ResolvedUniversity>of()) {
                if (university == null || university.id() == null) {
                    continue;
                }
                if (matchesUniversity(mention, university)) {
                    byId.putIfAbsent(university.id(), new MemoryUniversityRef(university.id(), university.name(), university.acronym()));
                }
            }
        }
        return new ArrayList<>(byId.values());
    }

    private List<MemoryUniversityRef> applyUniversityDelta(List<MemoryUniversityRef> current, List<String> add, List<String> remove, GraduateRouteExecutionResult result) {
        Map<Long, MemoryUniversityRef> byId = new LinkedHashMap<>();
        if (current != null) {
            for (MemoryUniversityRef ref : current) {
                if (ref != null && ref.id() != null) {
                    byId.putIfAbsent(ref.id(), ref);
                }
            }
        }
        for (MemoryUniversityRef ref : resolveUniversities(add, result)) {
            if (ref != null && ref.id() != null) {
                byId.putIfAbsent(ref.id(), ref);
            }
        }
        Set<Long> removeIds = universityIds(remove, result);
        byId.keySet().removeIf(removeIds::contains);
        return new ArrayList<>(byId.values());
    }

    private Set<Long> universityIds(List<String> mentions, GraduateRouteExecutionResult result) {
        Set<Long> ids = new LinkedHashSet<>();
        if (mentions == null) {
            return ids;
        }
        for (MemoryUniversityRef ref : resolveUniversities(mentions, result)) {
            if (ref.id() != null) {
                ids.add(ref.id());
            }
        }
        return ids;
    }

    private List<String> applyDegreeDelta(List<String> current, List<String> add, List<String> remove, GraduateRouteExecutionResult result) {
        Set<String> degrees = new LinkedHashSet<>(normalizeDegrees(current));
        degrees.addAll(normalizeDegrees(add));
        Set<String> removeSet = new LinkedHashSet<>(normalizeDegrees(remove));
        degrees.removeIf(removeSet::contains);
        if (degrees.isEmpty() && result != null) {
            degrees.addAll(normalizeDegrees(extractDegrees(result)));
        }
        return new ArrayList<>(degrees);
    }

    private List<String> normalizeDegrees(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                normalized.add(value.trim().toUpperCase(Locale.ROOT));
            }
        }
        return new ArrayList<>(normalized);
    }

    private List<String> applyStringDelta(List<String> current, List<String> add, List<String> remove) {
        Set<String> values = new LinkedHashSet<>();
        if (current != null) {
            for (String value : current) {
                if (value != null && !value.isBlank()) {
                    values.add(value.trim());
                }
            }
        }
        if (add != null) {
            for (String value : add) {
                if (value != null && !value.isBlank()) {
                    values.add(value.trim());
                }
            }
        }
        Set<String> removeSet = new LinkedHashSet<>();
        if (remove != null) {
            for (String value : remove) {
                if (value != null && !value.isBlank()) {
                    removeSet.add(value.trim());
                }
            }
        }
        values.removeIf(removeSet::contains);
        return new ArrayList<>(values);
    }

    private List<MemoryUniversityRef> mergeComparisonUniversities(List<MemoryUniversityRef> current, GraduateRouteExecutionResult result) {
        Map<Long, MemoryUniversityRef> byId = new LinkedHashMap<>();
        if (current != null) {
            for (MemoryUniversityRef ref : current) {
                if (ref != null && ref.id() != null) {
                    byId.putIfAbsent(ref.id(), ref);
                }
            }
        }
        if (result != null) {
            for (ResolvedUniversity university : result.resolvedUniversities()) {
                if (university != null && university.id() != null) {
                    byId.putIfAbsent(university.id(), new MemoryUniversityRef(university.id(), university.name(), university.acronym()));
                }
            }
        }
        return new ArrayList<>(byId.values());
    }

    private List<MemoryUniversityRef> resolvedUniversities(List<com.uniai.chat.application.retrieval.ResolvedUniversity> universities) {
        if (universities == null || universities.isEmpty()) {
            return List.of();
        }
        List<MemoryUniversityRef> refs = new ArrayList<>();
        for (ResolvedUniversity university : universities) {
            if (university != null && university.id() != null) {
                refs.add(new MemoryUniversityRef(university.id(), university.name(), university.acronym()));
            }
        }
        return refs;
    }

    private boolean matchesUniversity(String mention, ResolvedUniversity university) {
        if (mention == null || mention.isBlank() || university == null) {
            return false;
        }
        String normalized = mention.trim().toLowerCase(Locale.ROOT);
        if (university.acronym() != null && normalized.contains(university.acronym().trim().toLowerCase(Locale.ROOT))) {
            return true;
        }
        if (university.name() != null && normalized.contains(university.name().trim().toLowerCase(Locale.ROOT))) {
            return true;
        }
        return false;
    }

    private List<String> extractDegrees(GraduateRouteExecutionResult result) {
        if (result == null || result.canonicalArguments() == null) return List.of();
        var value = result.canonicalArguments().get("degreeType");
        if (value != null && value.isTextual()) return List.of(value.textValue());
        value = result.canonicalArguments().get("degreeTypes");
        if (value == null || !value.isArray()) return List.of();
        List<String> degrees = new ArrayList<>();
        value.forEach(item -> { if (item.isTextual()) degrees.add(item.textValue()); });
        return List.copyOf(degrees);
    }
}
