package com.uniai.shared.exception;

public class InvalidEmailOrPassword extends RuntimeException {
    public InvalidEmailOrPassword() {
        super("Invalid email or password!");
    }
}
