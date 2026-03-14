package com.uniai.feedback.presentation.controller;

import com.uniai.feedback.application.dto.command.SubmitFeedbackCommand;
import com.uniai.feedback.application.port.in.SubmitFeedbackUseCase;
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

    private final SubmitFeedbackUseCase submitFeedbackUseCase;

    @PostMapping
    public ResponseEntity<Void> submitFeedback(@RequestBody SubmitFeedbackCommand command) {
        submitFeedbackUseCase.submitFeedback(command);
        return ResponseEntity.ok().build();
    }
}
