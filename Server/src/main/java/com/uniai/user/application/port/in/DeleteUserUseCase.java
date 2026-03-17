package com.uniai.user.application.port.in;

import com.uniai.user.application.dto.command.DeleteUserCommand;

public interface DeleteUserUseCase {
    /** Deletes the account of the authenticated user after verifying their password. */
    void deleteUser(String email, DeleteUserCommand command);
}
