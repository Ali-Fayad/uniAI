package com.uniai.user.application.port.out;

import com.uniai.user.domain.valueobject.VerificationCodeType;

/**
 * Outbound port — implemented in infrastructure.
 * Responsible solely for dispatching notification emails to users.
 */
public interface NotificationPort {
    /**
     * Sends a verification email containing the provided OTP code.
     *
     * @param toEmail recipient email address
     * @param type    determines the email template and subject
     * @param code    the OTP code to include
     */
    void sendVerificationEmail(String toEmail, VerificationCodeType type, String code);
}
