package com.uniai.cvbuilder.domain.builder;

import com.uniai.cvbuilder.domain.model.Skill;

public final class SkillBuilder {

    private final Skill.SkillBuilder builder;

    private SkillBuilder(Long cvId, String name) {
        this.builder = Skill.builder()
                .cvId(cvId)
                .name(name);
    }

    public static SkillBuilder newSkill(Long cvId, String name) {
        return new SkillBuilder(cvId, name);
    }

    public SkillBuilder level(String level) {
        builder.level(level);
        return this;
    }

    public SkillBuilder order(Integer order) {
        builder.order(order);
        return this;
    }

    public Skill build() {
        return builder.build();
    }
}
