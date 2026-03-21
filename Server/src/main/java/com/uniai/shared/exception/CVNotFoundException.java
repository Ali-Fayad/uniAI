package com.uniai.shared.exception;

public class CVNotFoundException extends RuntimeException {
    public CVNotFoundException(String message) {
        super(message);
    }

    public CVNotFoundException() {
        super("CV not found");
    }
}
