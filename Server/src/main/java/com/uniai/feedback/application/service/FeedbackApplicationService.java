package com.uniai.feedback.application.service;

import com.uniai.feedback.application.dto.command.SubmitFeedbackCommand;
import com.uniai.feedback.application.port.in.SubmitFeedbackUseCase;
import com.uniai.feedback.domain.model.Feedback;
import com.uniai.feedback.domain.repository.FeedbackRepository;
import com.uniai.shared.exception.FeedbackNotValidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Application service for feedback submission.
 */
@Service
@RequiredArgsConstructor
public class FeedbackApplicationService implements SubmitFeedbackUseCase {

    private final FeedbackRepository feedbackRepository;

    @Override
    public void submitFeedback(Long userId, SubmitFeedbackCommand command) {
        if (userId == null
                || command.getContent() == null
                || command.getContent().isBlank()) {
            throw new FeedbackNotValidException("Feedback is not valid");
        }

        Integer rating = command.getRating();
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new FeedbackNotValidException("Feedback rating must be between 1 and 5");
        }

        Feedback feedback = Feedback.builder()
                .userId(userId)
                .content(command.getContent())
                .rating(rating)
                .build();

        feedbackRepository.save(feedback);
    }
}
