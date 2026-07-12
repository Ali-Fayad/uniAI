package com.uniai.chat.application.provider;

public enum AiProviderFailureCategory {
    NONE(false),
    MISCONFIGURED(false),
    UNAVAILABLE(true),
    TIMEOUT(true),
    RATE_LIMITED(true),
    HTTP_CLIENT_ERROR(false),
    HTTP_SERVER_ERROR(true),
    INVALID_RESPONSE(true),
    EMPTY_RESPONSE(true),
    UNKNOWN(false);

    private final boolean retryable;

    AiProviderFailureCategory(boolean retryable) {
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
