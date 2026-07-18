package com.uniai.chat.application.retrieval;

public record GraduateKnowledgeReference(
        GraduateKnowledgeReferenceKind kind,
        String logicalName,
        String acronym,
        Integer renderedOrdinal
) {
    public GraduateKnowledgeReference {
        kind = kind == null ? GraduateKnowledgeReferenceKind.UNIVERSITY : kind;
        logicalName = logicalName == null || logicalName.isBlank() ? null : logicalName.trim();
        acronym = acronym == null || acronym.isBlank() ? null : acronym.trim();
        if (logicalName == null && acronym == null) {
            throw new IllegalArgumentException("A reference requires a logical name or acronym");
        }
        if (renderedOrdinal != null && renderedOrdinal < 1) {
            throw new IllegalArgumentException("Rendered ordinal must be positive");
        }
    }
}
