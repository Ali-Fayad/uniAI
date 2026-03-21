package com.uniai.cvbuilder.infrastructure.persistence.repository;

import com.uniai.cvbuilder.domain.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaSkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByCvId(Long cvId);

    void deleteByCvId(Long cvId);
}
