package com.uniai.cvbuilder.infrastructure.persistence.adapter;

import com.uniai.cvbuilder.domain.model.Education;
import com.uniai.cvbuilder.domain.repository.EducationRepository;
import com.uniai.cvbuilder.infrastructure.persistence.repository.JpaEducationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-backed implementation of {@link EducationRepository}.
 */
@Repository
@RequiredArgsConstructor
public class EducationRepositoryAdapter implements EducationRepository {

    private final JpaEducationRepository jpaRepository;

    @Override
    public Optional<Education> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Education> findByCvId(Long cvId) {
        return jpaRepository.findByCvId(cvId);
    }

    @Override
    public Education save(Education education) {
        return jpaRepository.save(education);
    }

    @Override
    public void delete(Education education) {
        jpaRepository.delete(education);
    }

    @Override
    public void deleteByCvId(Long cvId) {
        jpaRepository.deleteByCvId(cvId);
    }
}
