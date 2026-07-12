package com.uniai.chat.application.provider;

import java.time.Instant;
import java.util.Locale;

public record AiProviderStatusSnapshot(
        String provider,
        String model,
        AiProviderRuntimeStatus status,
        AiProviderFailureCategory lastFailureCategory,
        Instant lastSuccessAt,
        Instant lastFailureAt,
        Long lastLatencyMs
) {
    public AiProviderStatusSnapshot {
        provider = normalize(provider);
        model = normalize(model);
        status = status == null ? AiProviderRuntimeStatus.UNKNOWN : status;
        lastFailureCategory = lastFailureCategory == null ? AiProviderFailureCategory.UNKNOWN : lastFailureCategory;
    }

    public static AiProviderStatusSnapshot unknown(String provider) {
        return new AiProviderStatusSnapshot(
                provider,
                "",
                AiProviderRuntimeStatus.UNKNOWN,
                AiProviderFailureCategory.UNKNOWN,
                null,
                null,
                null
        );
    }

    public AiProviderStatusSnapshot withSuccess(String model, long latencyMs, Instant observedAt) {
        return new AiProviderStatusSnapshot(
                provider,
                model,
                AiProviderRuntimeStatus.AVAILABLE,
                AiProviderFailureCategory.NONE,
                observedAt,
                lastFailureAt,
                latencyMs
        );
    }

    public AiProviderStatusSnapshot withFailure(
            String model,
            AiProviderFailureCategory failureCategory,
            Instant observedAt,
            long latencyMs
    ) {
        AiProviderFailureCategory effectiveCategory = failureCategory == null
                ? AiProviderFailureCategory.UNKNOWN
                : failureCategory;
        return new AiProviderStatusSnapshot(
                provider,
                model,
                effectiveCategory == AiProviderFailureCategory.MISCONFIGURED
                        ? AiProviderRuntimeStatus.MISCONFIGURED
                        : AiProviderRuntimeStatus.UNAVAILABLE,
                effectiveCategory,
                lastSuccessAt,
                observedAt,
                latencyMs
        );
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
