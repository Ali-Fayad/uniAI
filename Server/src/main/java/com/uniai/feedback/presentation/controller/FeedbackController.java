package com.uniai.feedback.presentation.controller;

import com.uniai.feedback.application.dto.command.SubmitFeedbackCommand;
import com.uniai.feedback.application.port.in.SubmitFeedbackUseCase;
import com.uniai.shared.infrastructure.jwt.JwtFacade;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles feedback submission.
 * Extracted from the old UserController to respect the Single Responsibility Principle.
 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final JwtFacade jwtFacade;
    private final UserRepository userRepository;
    private final SubmitFeedbackUseCase submitFeedbackUseCase;

    @PostMapping
    public ResponseEntity<Void> submitFeedback(@RequestBody SubmitFeedbackCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        Long userId = userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("Authentication required"));
        submitFeedbackUseCase.submitFeedback(userId, command);
        return ResponseEntity.ok().build();
    }
}
