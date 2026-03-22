package com.uniai.cvbuilder.infrastructure.persistence.adapter;

import com.uniai.cvbuilder.domain.model.Skill;
import com.uniai.cvbuilder.domain.repository.SkillRepository;
import com.uniai.cvbuilder.infrastructure.persistence.repository.JpaSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-backed implementation of {@link SkillRepository}.
 */
@Repository
@RequiredArgsConstructor
public class SkillRepositoryAdapter implements SkillRepository {

    private final JpaSkillRepository jpaRepository;

    @Override
    public Optional<Skill> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Skill> findByCvId(Long cvId) {
        return jpaRepository.findByCvId(cvId);
    }

    @Override
    public Skill save(Skill skill) {
        return jpaRepository.save(skill);
    }

    @Override
    public void delete(Skill skill) {
        jpaRepository.delete(skill);
    }

    @Override
    public void deleteByCvId(Long cvId) {
        jpaRepository.deleteByCvId(cvId);
    }
}
