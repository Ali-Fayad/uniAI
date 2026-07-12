package com.uniai.chat.infrastructure.ai;

import com.uniai.chat.application.port.out.AiProviderStatusPort;
import com.uniai.chat.application.provider.AiProviderFailureCategory;
import com.uniai.chat.application.provider.AiProviderStatusSnapshot;

import java.time.Instant;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAiProviderStatusRegistry implements AiProviderStatusPort {

    private final ConcurrentHashMap<String, AiProviderStatusSnapshot> snapshots = new ConcurrentHashMap<>();

    @Override
    public void recordSuccess(String provider, String model, long latencyMs) {
        String normalizedProvider = normalizeProvider(provider);
        if (normalizedProvider == null) {
            return;
        }
        Instant now = Instant.now();
        snapshots.compute(normalizedProvider, (key, existing) -> existing == null
                ? AiProviderStatusSnapshot.unknown(normalizedProvider).withSuccess(model, latencyMs, now)
                : existing.withSuccess(model, latencyMs, now));
    }

    @Override
    public void recordFailure(String provider, String model, AiProviderFailureCategory failureCategory, long latencyMs) {
        String normalizedProvider = normalizeProvider(provider);
        if (normalizedProvider == null) {
            return;
        }
        Instant now = Instant.now();
        snapshots.compute(normalizedProvider, (key, existing) -> existing == null
                ? AiProviderStatusSnapshot.unknown(normalizedProvider).withFailure(model, failureCategory, now, latencyMs)
                : existing.withFailure(model, failureCategory, now, latencyMs));
    }

    @Override
    public AiProviderStatusSnapshot getStatus(String provider) {
        String normalizedProvider = normalizeProvider(provider);
        if (normalizedProvider == null) {
            return AiProviderStatusSnapshot.unknown("");
        }
        return snapshots.getOrDefault(normalizedProvider, AiProviderStatusSnapshot.unknown(normalizedProvider));
    }

    @Override
    public Map<String, AiProviderStatusSnapshot> getAllStatuses() {
        return Map.copyOf(snapshots);
    }

    private String normalizeProvider(String provider) {
        if (provider == null) {
            return null;
        }
        String normalized = provider.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
