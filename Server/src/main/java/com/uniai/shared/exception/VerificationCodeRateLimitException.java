package com.uniai.shared.exception;

public class VerificationCodeRateLimitException extends RuntimeException {
    public VerificationCodeRateLimitException(String message) {
        super(message);
    }
}
