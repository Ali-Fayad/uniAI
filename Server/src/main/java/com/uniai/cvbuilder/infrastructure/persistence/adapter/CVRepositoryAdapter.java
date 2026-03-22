package com.uniai.cvbuilder.infrastructure.persistence.adapter;

import com.uniai.cvbuilder.domain.model.CV;
import com.uniai.cvbuilder.domain.repository.CVRepository;
import com.uniai.cvbuilder.infrastructure.persistence.repository.JpaCVRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-backed implementation of {@link CVRepository}.
 */
@Repository
@RequiredArgsConstructor
public class CVRepositoryAdapter implements CVRepository {

    private final JpaCVRepository jpaRepository;

    @Override
    public Optional<CV> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<CV> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<CV> findDefaultByUserId(Long userId) {
        return jpaRepository.findFirstByUserIdAndIsDefaultTrue(userId);
    }

    @Override
    public CV save(CV cv) {
        return jpaRepository.save(cv);
    }

    @Override
    public void delete(CV cv) {
        jpaRepository.delete(cv);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
