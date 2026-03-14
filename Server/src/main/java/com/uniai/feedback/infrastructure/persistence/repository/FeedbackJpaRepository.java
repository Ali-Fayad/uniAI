package com.uniai.feedback.infrastructure.persistence.repository;

import com.uniai.feedback.domain.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackJpaRepository extends JpaRepository<Feedback, Long> {
}
