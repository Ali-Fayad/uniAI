package com.uniai.chat.application.retrieval;

public record GraduateKnowledgeFollowUpContext(
        Long referencedUniversityId,
        Integer referencedResultOrdinal,
        GraduateKnowledgeResource inheritedResource,
        GraduateKnowledgeOperation inheritedOperation
) {
    public GraduateKnowledgeFollowUpContext {
        if (referencedResultOrdinal != null && referencedResultOrdinal < 1) {
            throw new IllegalArgumentException("Referenced result ordinal must be positive");
        }
        inheritedResource = inheritedResource == null ? GraduateKnowledgeResource.NONE : inheritedResource;
        inheritedOperation = inheritedOperation == null ? GraduateKnowledgeOperation.NONE : inheritedOperation;
        if (!GraduateKnowledgeQuery.isCompatible(inheritedResource, inheritedOperation)) {
            throw new IllegalArgumentException("Unsupported inherited resource/operation combination");
        }
    }

    public static GraduateKnowledgeFollowUpContext empty() {
        return new GraduateKnowledgeFollowUpContext(null, null, GraduateKnowledgeResource.NONE, GraduateKnowledgeOperation.NONE);
    }
}
