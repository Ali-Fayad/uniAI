package com.uniai.user.application.port.in;

import com.uniai.user.application.dto.command.SignUpCommand;
import com.uniai.user.application.dto.response.SignUpResultDto;

public interface SignUpUseCase {
    /** Returns a verification-required result for the expected signup flow. */
    SignUpResultDto signUp(SignUpCommand command);
}
