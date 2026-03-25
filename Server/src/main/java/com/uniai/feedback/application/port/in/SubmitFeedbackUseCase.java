package com.uniai.feedback.application.port.in;

import com.uniai.feedback.application.dto.command.SubmitFeedbackCommand;

public interface SubmitFeedbackUseCase {
    void submitFeedback(Long userId, SubmitFeedbackCommand command);
}
