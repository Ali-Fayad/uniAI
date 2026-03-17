package com.uniai.user.application.port.in;

import com.uniai.user.application.dto.command.SignUpCommand;

public interface SignUpUseCase {
    /** Returns a JWT if already verified, throws VerificationNeededException otherwise. */
    String signUp(SignUpCommand command);
}
