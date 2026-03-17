package com.uniai.user.application.port.in;

import com.uniai.user.application.dto.command.RequestPasswordCommand;

public interface ConfirmPasswordResetUseCase {
    /** Validates the OTP, resets the password, and returns a new JWT. */
    String confirmPasswordReset(RequestPasswordCommand command);
}
