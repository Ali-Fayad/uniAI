package com.uniai.feedback.application.service;

import com.uniai.feedback.application.dto.command.SubmitFeedbackCommand;
import com.uniai.feedback.application.port.in.SubmitFeedbackUseCase;
import com.uniai.feedback.domain.model.Feedback;
import com.uniai.feedback.domain.repository.FeedbackRepository;
import com.uniai.shared.exception.FeedbackNotValidException;
import com.uniai.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Application service for feedback submission.
 */
@Service
@RequiredArgsConstructor
public class FeedbackApplicationService implements SubmitFeedbackUseCase {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    @Override
    public void submitFeedback(SubmitFeedbackCommand command) {
        if (!userRepository.existsByEmail(command.getEmail())
                || command.getComment() == null
                || command.getComment().isBlank()) {
            throw new FeedbackNotValidException("Feedback is not valid");
        }

        Feedback feedback = Feedback.builder()
                .email(command.getEmail())
                .comment(command.getComment())
                .build();

        feedbackRepository.save(feedback);
    }
}
