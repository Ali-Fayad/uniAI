package com.uniai.cvbuilder.domain.repository;

import com.uniai.cvbuilder.domain.model.Skill;

import java.util.List;
import java.util.Optional;

public interface SkillRepository {

    Optional<Skill> findById(Long id);

    List<Skill> findByCvId(Long cvId);

    Skill save(Skill skill);

    void delete(Skill skill);

    void deleteByCvId(Long cvId);
}
