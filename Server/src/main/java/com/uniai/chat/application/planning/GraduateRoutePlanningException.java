package com.uniai.chat.application.planning;

/** A safe contract or dispatch failure raised before any retrieval is executed. */
public class GraduateRoutePlanningException extends RuntimeException {
    public GraduateRoutePlanningException(String message) {
        super(message);
    }

    public GraduateRoutePlanningException(String message, Throwable cause) {
        super(message, cause);
    }
}
