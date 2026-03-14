package com.uniai.user.application.port.in;

import com.uniai.user.application.dto.command.SignInCommand;

public interface SignInUseCase {
    /** Returns a JWT on success. Throws VerificationNeededException or UnauthorizedAccessException as needed. */
    String signIn(SignInCommand command);
}
