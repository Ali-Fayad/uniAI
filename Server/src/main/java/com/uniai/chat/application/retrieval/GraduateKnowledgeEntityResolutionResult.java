package com.uniai.chat.application.retrieval;

import java.util.List;

public record GraduateKnowledgeEntityResolutionResult(
        GraduateKnowledgeEntityResolutionStatus status,
        List<ResolvedUniversity> universities,
        List<String> unresolvedReferences,
        boolean explicitReference
) {
    public GraduateKnowledgeEntityResolutionResult {
        status = status == null ? GraduateKnowledgeEntityResolutionStatus.NONE_REQUESTED : status;
        universities = universities == null ? List.of() : List.copyOf(universities);
        unresolvedReferences = unresolvedReferences == null ? List.of() : List.copyOf(unresolvedReferences);
    }

    public boolean isScoped() {
        return explicitReference || !universities.isEmpty() || !unresolvedReferences.isEmpty();
    }
}
