package com.uniai.cvbuilder.domain.builder;

import com.uniai.cvbuilder.domain.model.Language;

/**
 * Fluent builder for constructing {@link Language} aggregates with optional proficiency levels.
 */
public final class LanguageBuilder {

    private final Language.LanguageBuilder builder;

    private LanguageBuilder(Long cvId, String name) {
        this.builder = Language.builder()
                .cvId(cvId)
                .name(name);
    }

    public static LanguageBuilder newLanguage(Long cvId, String name) {
        return new LanguageBuilder(cvId, name);
    }

    public LanguageBuilder proficiency(String proficiency) {
        builder.proficiency(proficiency);
        return this;
    }

    public Language build() {
        return builder.build();
    }
}
