package com.uniai.shared.exception;

public class SelfDeleteNotAllowedException extends RuntimeException {
    public SelfDeleteNotAllowedException() {
        super("Self deletion is not allowed");
    }
}
