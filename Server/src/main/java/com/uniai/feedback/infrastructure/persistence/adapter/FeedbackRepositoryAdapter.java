package com.uniai.feedback.infrastructure.persistence.adapter;

import com.uniai.feedback.domain.model.Feedback;
import com.uniai.feedback.domain.repository.FeedbackRepository;
import com.uniai.feedback.infrastructure.persistence.repository.FeedbackJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-backed implementation of the domain {@link FeedbackRepository} interface.
 */
@Repository
@RequiredArgsConstructor
public class FeedbackRepositoryAdapter implements FeedbackRepository {

    private final FeedbackJpaRepository jpaRepository;

    @Override
    public Feedback save(Feedback feedback) {
        return jpaRepository.save(feedback);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Feedback> findAllByOrderByCreatedAtDesc() {
        return jpaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Optional<Feedback> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
