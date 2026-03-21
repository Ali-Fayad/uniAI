package com.uniai.shared.exception;

public class UniversityNotFoundException extends RuntimeException {
    public UniversityNotFoundException(String message) {
        super(message);
    }

    public UniversityNotFoundException() {
        super("University not found");
    }
}
