package com.uniai.user.domain.valueobject;

/**
 * Represents different types of verification codes sent to users.
 * Email content per type is configured via EmailProperties.
 */
public enum VerificationCodeType {
    VERIFY,
    TWO_FACT_AUTH,
    CHANGE_PASSWORD
}
