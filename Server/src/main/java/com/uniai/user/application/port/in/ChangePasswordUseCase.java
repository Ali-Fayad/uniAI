package com.uniai.user.application.port.in;

import com.uniai.user.application.dto.command.ChangePasswordCommand;

public interface ChangePasswordUseCase {
    /** Changes the password of the authenticated user after verifying the current password. */
    void changePassword(String email, ChangePasswordCommand command);
}
