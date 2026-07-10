package com.uniai.chat.application.retrieval;

import java.util.List;

public record GraduateKnowledgeQuery(
        GraduateKnowledgeIntent intent,
        List<ResolvedUniversity> resolvedUniversities,
        List<String> degreeTypes,
        GraduateProgramDetailLevel detailLevel,
        boolean followUpResolved,
        boolean ambiguous
) {
    public GraduateKnowledgeQuery {
        intent = intent == null ? GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS : intent;
        resolvedUniversities = resolvedUniversities == null ? List.of() : List.copyOf(resolvedUniversities);
        degreeTypes = degreeTypes == null ? List.of() : List.copyOf(degreeTypes);
        if (detailLevel == null && intent == GraduateKnowledgeIntent.PROGRAM_LOOKUP) {
            detailLevel = GraduateProgramDetailLevel.LIST;
        }
    }

    public boolean hasResolvedUniversities() {
        return !resolvedUniversities.isEmpty();
    }

    public boolean hasDegreeTypes() {
        return !degreeTypes.isEmpty();
    }
}
