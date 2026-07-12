package com.uniai.chat.application.memory;

import com.uniai.chat.application.interpretation.GraduateQueryInterpretationResult;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationStatus;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.ResolvedUniversity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ConversationMemoryTriggerPolicy {

    private static final int SAFETY_REFRESH_TURN_INTERVAL = 5;

    public boolean shouldUpdate(
            ConversationMemory previousMemory,
            GraduateQueryInterpretationResult interpretationResult,
            long completedTurnCount,
            String currentUserMessage
    ) {
        if (interpretationResult == null) {
            return false;
        }

        if (completedTurnCount > 0 && completedTurnCount % SAFETY_REFRESH_TURN_INTERVAL == 0) {
            return true;
        }

        if (interpretationResult.status() != GraduateQueryInterpretationStatus.VALID
                && interpretationResult.status() != GraduateQueryInterpretationStatus.FALLBACK_USED) {
            return false;
        }

        GraduateKnowledgeQuery query = interpretationResult.query();
        if (query == null) {
            return false;
        }

        ConversationMemory memory = previousMemory == null ? ConversationMemory.empty() : previousMemory;
        if (!sameIntent(memory.lastIntentEnum(), query.intent())) {
            return true;
        }

        Set<Long> previousIds = universityIds(memory.activeUniversities());
        Set<Long> currentIds = universityIdsResolved(query.resolvedUniversities());
        if (!previousIds.equals(currentIds)) {
            return true;
        }

        if (!sameDegrees(memory.activeDegreeTypes(), query.degreeTypes())) {
            return true;
        }

        boolean currentComparison = query.followUpResolved() && query.resolvedUniversities().size() > 1;
        if (memory.comparisonActive() != currentComparison) {
            return true;
        }

        return containsMemorySignal(currentUserMessage);
    }

    private boolean sameIntent(com.uniai.chat.application.retrieval.GraduateKnowledgeIntent left,
                               com.uniai.chat.application.retrieval.GraduateKnowledgeIntent right) {
        return left == right;
    }

    private Set<Long> universityIds(List<MemoryUniversityRef> universities) {
        Set<Long> ids = new HashSet<>();
        if (universities == null) {
            return ids;
        }
        for (MemoryUniversityRef ref : universities) {
            if (ref != null && ref.id() != null) {
                ids.add(ref.id());
            }
        }
        return ids;
    }

    private Set<Long> universityIdsResolved(List<ResolvedUniversity> universities) {
        Set<Long> ids = new HashSet<>();
        if (universities == null) {
            return ids;
        }
        for (ResolvedUniversity university : universities) {
            if (university != null && university.id() != null) {
                ids.add(university.id());
            }
        }
        return ids;
    }

    private Set<String> degreeSet(List<String> degrees) {
        Set<String> values = new HashSet<>();
        if (degrees == null) {
            return values;
        }
        for (String degree : degrees) {
            if (degree != null && !degree.isBlank()) {
                values.add(degree.trim().toUpperCase());
            }
        }
        return values;
    }

    private boolean sameDegrees(List<String> previous, List<String> current) {
        return degreeSet(previous).equals(degreeSet(current));
    }

    private boolean containsMemorySignal(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String normalized = message.trim().toLowerCase();
        return normalized.contains("prefer")
                || normalized.contains("preference")
                || normalized.contains("preferably")
                || normalized.contains("correction")
                || normalized.contains("actually")
                || normalized.contains("instead")
                || normalized.contains("online")
                || normalized.contains("on campus")
                || normalized.contains("hybrid")
                || normalized.contains("cheaper")
                || normalized.contains("same")
                || normalized.contains("compare")
                || normalized.contains("comparison");
    }
}
