package com.uniai.shared.exception;

public class FeedbackNotFoundException extends RuntimeException {
    public FeedbackNotFoundException() {
        super("Feedback not found");
    }
}
