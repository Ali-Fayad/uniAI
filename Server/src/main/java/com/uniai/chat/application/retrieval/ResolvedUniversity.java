package com.uniai.chat.application.retrieval;

public record ResolvedUniversity(
        Long id,
        String name,
        String acronym
) {
    public ResolvedUniversity {
        name = name == null ? null : name.trim();
        acronym = acronym == null ? null : acronym.trim();
    }
}
