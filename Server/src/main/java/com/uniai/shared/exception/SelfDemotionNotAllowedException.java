package com.uniai.shared.exception;

public class SelfDemotionNotAllowedException extends RuntimeException {
    public SelfDemotionNotAllowedException() {
        super("Self demotion is not allowed");
    }
}
