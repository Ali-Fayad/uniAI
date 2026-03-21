package com.uniai.cvbuilder.infrastructure.persistence.adapter;

import com.uniai.cvbuilder.domain.model.Experience;
import com.uniai.cvbuilder.domain.repository.ExperienceRepository;
import com.uniai.cvbuilder.infrastructure.persistence.repository.JpaExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExperienceRepositoryAdapter implements ExperienceRepository {

    private final JpaExperienceRepository jpaRepository;

    @Override
    public Optional<Experience> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Experience> findByCvId(Long cvId) {
        return jpaRepository.findByCvId(cvId);
    }

    @Override
    public Experience save(Experience experience) {
        return jpaRepository.save(experience);
    }

    @Override
    public void delete(Experience experience) {
        jpaRepository.delete(experience);
    }

    @Override
    public void deleteByCvId(Long cvId) {
        jpaRepository.deleteByCvId(cvId);
    }
}
