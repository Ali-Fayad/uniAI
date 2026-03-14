package com.uniai.feedback.domain.repository;

import com.uniai.feedback.domain.model.Feedback;

/**
 * Domain repository interface for Feedback.
 * Implementations live in the infrastructure layer.
 */
public interface FeedbackRepository {

    Feedback save(Feedback feedback);
}
