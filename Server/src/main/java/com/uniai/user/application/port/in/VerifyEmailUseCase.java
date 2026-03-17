package com.uniai.user.application.port.in;

import com.uniai.user.application.dto.command.VerifyCommand;

public interface VerifyEmailUseCase {
    /** Verifies the email address with the OTP code and returns a JWT. */
    String verifyEmail(VerifyCommand command);
}
