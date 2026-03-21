package com.uniai.shared.exception;

public class SectionNotFoundException extends RuntimeException {
    public SectionNotFoundException(String message) {
        super(message);
    }

    public SectionNotFoundException() {
        super("Section not found");
    }
}
