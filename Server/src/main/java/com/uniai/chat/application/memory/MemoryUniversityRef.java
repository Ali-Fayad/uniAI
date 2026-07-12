package com.uniai.chat.application.memory;

public record MemoryUniversityRef(
        Long id,
        String name,
        String acronym
) {
    public MemoryUniversityRef {
        name = normalize(name);
        acronym = normalize(acronym);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
