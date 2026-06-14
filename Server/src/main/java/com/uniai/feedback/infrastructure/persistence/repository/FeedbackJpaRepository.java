package com.uniai.feedback.infrastructure.persistence.repository;

import com.uniai.feedback.domain.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackJpaRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Feedback> findAllByOrderByCreatedAtDesc();
    java.util.Optional<Feedback> findById(Long id);

    @Modifying
    @Query("DELETE FROM Feedback f WHERE f.userId = :userId")
    void deleteByUserId(Long userId);
}
