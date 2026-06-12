package com.uniai.shared.exception;

public class CVAccessDeniedException extends RuntimeException {
    public CVAccessDeniedException() {
        super("You do not have permission to access this CV");
    }

    public CVAccessDeniedException(String message) {
        super(message);
    }
}
