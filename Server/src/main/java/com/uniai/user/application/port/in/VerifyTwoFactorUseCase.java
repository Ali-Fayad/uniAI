package com.uniai.user.application.port.in;

import com.uniai.user.application.dto.command.VerifyCommand;

public interface VerifyTwoFactorUseCase {
    /** Verifies the 2FA code and returns a JWT. */
    String verifyTwoFactor(VerifyCommand command);
}
