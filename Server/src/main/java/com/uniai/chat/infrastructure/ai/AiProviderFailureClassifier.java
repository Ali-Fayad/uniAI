package com.uniai.chat.infrastructure.ai;

import com.uniai.chat.application.provider.AiProviderFailureCategory;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;

final class AiProviderFailureClassifier {

    private AiProviderFailureClassifier() {
    }

    static AiProviderFailureCategory classifyHttpStatus(int statusCode) {
        if (statusCode == 429) {
            return AiProviderFailureCategory.RATE_LIMITED;
        }
        if (statusCode == 401 || statusCode == 403) {
            return AiProviderFailureCategory.MISCONFIGURED;
        }
        if (statusCode >= 400 && statusCode < 500) {
            return AiProviderFailureCategory.HTTP_CLIENT_ERROR;
        }
        if (statusCode >= 500 && statusCode < 600) {
            return AiProviderFailureCategory.HTTP_SERVER_ERROR;
        }
        return AiProviderFailureCategory.UNKNOWN;
    }

    static AiProviderFailureCategory classifyThrowable(Throwable throwable) {
        if (throwable == null) {
            return AiProviderFailureCategory.UNKNOWN;
        }
        if (containsCause(throwable, SocketTimeoutException.class) || containsCause(throwable, InterruptedIOException.class)) {
            return AiProviderFailureCategory.TIMEOUT;
        }
        if (containsCause(throwable, UnknownHostException.class) || containsCause(throwable, ConnectException.class)) {
            return AiProviderFailureCategory.UNAVAILABLE;
        }
        String message = throwable.getMessage();
        if (message != null) {
            String normalized = message.toLowerCase(Locale.ROOT);
            if (normalized.contains("connection refused")
                    || normalized.contains("connection reset")
                    || normalized.contains("connection aborted")
                    || normalized.contains("host is unreachable")
                    || normalized.contains("no route to host")
                    || normalized.contains("broken pipe")) {
                return AiProviderFailureCategory.UNAVAILABLE;
            }
            if (normalized.contains("timed out")) {
                return AiProviderFailureCategory.TIMEOUT;
            }
            if (normalized.contains("invalid url") || normalized.contains("uri") || normalized.contains("malformed")) {
                return AiProviderFailureCategory.MISCONFIGURED;
            }
        }
        return AiProviderFailureCategory.UNKNOWN;
    }

    static AiProviderFailureCategory classifyParseFailure() {
        return AiProviderFailureCategory.INVALID_RESPONSE;
    }

    static AiProviderFailureCategory classifyEmptyResponse() {
        return AiProviderFailureCategory.EMPTY_RESPONSE;
    }

    static boolean shouldMarkRuntimeUnavailable(AiProviderFailureCategory category) {
        return category != AiProviderFailureCategory.MISCONFIGURED;
    }

    private static boolean containsCause(Throwable throwable, Class<? extends Throwable> targetType) {
        Throwable current = throwable;
        while (current != null) {
            if (targetType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
