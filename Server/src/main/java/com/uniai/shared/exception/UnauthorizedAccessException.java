package com.uniai.shared.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException() {
        super("Unauthorized access");
    }

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
