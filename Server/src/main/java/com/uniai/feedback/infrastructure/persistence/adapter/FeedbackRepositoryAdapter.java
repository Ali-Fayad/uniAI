package com.uniai.feedback.infrastructure.persistence.adapter;

import com.uniai.feedback.domain.model.Feedback;
import com.uniai.feedback.domain.repository.FeedbackRepository;
import com.uniai.feedback.infrastructure.persistence.repository.FeedbackJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
