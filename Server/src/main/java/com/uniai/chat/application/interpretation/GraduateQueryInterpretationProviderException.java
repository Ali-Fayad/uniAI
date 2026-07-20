package com.uniai.chat.application.interpretation;

/** Identifies provider-side failures before a usable canonical draft exists. */
public class GraduateQueryInterpretationProviderException extends IllegalStateException {
    private final String failureCategory;

    public GraduateQueryInterpretationProviderException(String message, String failureCategory) {
        super(message);
        this.failureCategory = failureCategory == null || failureCategory.isBlank()
                ? "AI_QUERY_INTERPRETATION_PROVIDER_FAILURE"
                : failureCategory;
    }

    public GraduateQueryInterpretationProviderException(String message, String failureCategory, Throwable cause) {
        super(message, cause);
        this.failureCategory = failureCategory == null || failureCategory.isBlank()
                ? "AI_QUERY_INTERPRETATION_PROVIDER_FAILURE"
                : failureCategory;
    }

    public String failureCategory() {
        return failureCategory;
    }
}
