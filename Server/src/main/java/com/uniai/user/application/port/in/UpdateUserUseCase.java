package com.uniai.user.application.port.in;

import com.uniai.user.application.dto.command.UpdateUserCommand;
import com.uniai.user.application.dto.response.AuthResponseDto;

public interface UpdateUserUseCase {
    /** Partially updates the user profile identified by email and returns the updated profile. */
    AuthResponseDto updateUser(String email, UpdateUserCommand command);
}
