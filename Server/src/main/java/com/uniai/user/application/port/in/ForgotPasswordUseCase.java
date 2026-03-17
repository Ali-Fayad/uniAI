package com.uniai.user.application.port.in;

public interface ForgotPasswordUseCase {
    /** Sends a password-reset OTP email to the given address. */
    void forgotPassword(String email);
}
