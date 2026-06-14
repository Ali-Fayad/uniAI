package com.uniai.feedback.domain.repository;

import com.uniai.feedback.domain.model.Feedback;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for Feedback.
 * Implementations live in the infrastructure layer.
 */
public interface FeedbackRepository {

    Feedback save(Feedback feedback);

    long count();

    List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Feedback> findAllByOrderByCreatedAtDesc();

    Optional<Feedback> findById(Long id);

    void deleteByUserId(Long userId);

    void deleteById(Long id);
}
