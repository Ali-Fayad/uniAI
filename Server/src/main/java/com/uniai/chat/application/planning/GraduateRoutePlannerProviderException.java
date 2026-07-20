package com.uniai.chat.application.planning;

public class GraduateRoutePlannerProviderException extends RuntimeException {
    private final String failureCategory;

    public GraduateRoutePlannerProviderException(String message, String failureCategory) {
        super(message);
        this.failureCategory = failureCategory;
    }

    public GraduateRoutePlannerProviderException(String message, String failureCategory, Throwable cause) {
        super(message, cause);
        this.failureCategory = failureCategory;
    }

    public String failureCategory() {
        return failureCategory;
    }
}
