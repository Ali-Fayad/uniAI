package com.uniai.shared.exception;

public class CVTemplateNotFoundException extends RuntimeException {
    public CVTemplateNotFoundException(String message) {
        super(message);
    }

    public CVTemplateNotFoundException() {
        super("CV template not found");
    }
}
