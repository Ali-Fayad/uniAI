package com.uniai.exception;

/**
 * Unchecked exception used to represent any error during Google OAuth processing.
 * Application can map this in a centralized exception handler to return proper HTTP responses.
 */
public class GoogleAuthException extends RuntimeException {
    public GoogleAuthException(String message) {
        super(message);
    }

    public GoogleAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}