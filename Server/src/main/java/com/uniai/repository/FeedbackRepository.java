package com.uniai.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uniai.model.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {


}
