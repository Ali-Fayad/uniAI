package com. uniai.exception;

public class InvalidMessageException extends RuntimeException {
    public InvalidMessageException() {
        super("Invalid message");
    }

    public InvalidMessageException(String message) {
        super(message);
    }
}