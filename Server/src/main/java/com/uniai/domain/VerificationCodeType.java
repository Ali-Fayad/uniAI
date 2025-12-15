package com.uniai.domain;

/**
 * Enum representing different types of verification codes.
 * Email content for each type is configured in email-messages.properties.
 */
public enum VerificationCodeType {
	/**
	 * Initial email verification when user signs up
	 */
	VERIFY,

	/**
	 * Two-factor authentication code for enhanced security
	 */
	TWO_FACT_AUTH,

	/**
	 * Password reset verification code
	 */
	CHANGE_PASSWORD
}
