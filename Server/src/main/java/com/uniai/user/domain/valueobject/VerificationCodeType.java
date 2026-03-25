package com.uniai.user.domain.valueobject;

/**
 * Represents different types of verification codes sent to users.
 * Email content per type is configured via EmailProperties.
 */
public enum VerificationCodeType {
    REGISTRATION,
    PASSWORD_RESET,
    EMAIL_CHANGE,
    TWO_FA
}
