package com.uniai.chat.application.retrieval;

import java.util.List;

public record GraduateKnowledgeFollowUpContext(
        Long referencedUniversityId,
        Integer referencedResultOrdinal,
        GraduateKnowledgeResource inheritedResource,
        GraduateKnowledgeOperation inheritedOperation,
        List<GraduateKnowledgeReference> references,
        GraduateKnowledgeComparisonDimension comparisonDimension
) {
    public GraduateKnowledgeFollowUpContext {
        if (referencedResultOrdinal != null && referencedResultOrdinal < 1) {
            throw new IllegalArgumentException("Referenced result ordinal must be positive");
        }
        inheritedResource = inheritedResource == null ? GraduateKnowledgeResource.NONE : inheritedResource;
        inheritedOperation = inheritedOperation == null ? GraduateKnowledgeOperation.NONE : inheritedOperation;
        references = references == null ? List.of() : List.copyOf(references);
        if (!GraduateKnowledgeQuery.isCompatible(inheritedResource, inheritedOperation)) {
            throw new IllegalArgumentException("Unsupported inherited resource/operation combination");
        }
    }

    public GraduateKnowledgeFollowUpContext(
            Long referencedUniversityId,
            Integer referencedResultOrdinal,
            GraduateKnowledgeResource inheritedResource,
            GraduateKnowledgeOperation inheritedOperation
    ) {
        this(referencedUniversityId, referencedResultOrdinal, inheritedResource, inheritedOperation, List.of(), null);
    }

    public static GraduateKnowledgeFollowUpContext empty() {
        return new GraduateKnowledgeFollowUpContext(null, null, GraduateKnowledgeResource.NONE, GraduateKnowledgeOperation.NONE, List.of(), null);
    }
}
