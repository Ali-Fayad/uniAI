package com.uniai.chat.application.port.out;

import com.uniai.chat.application.provider.AiProviderFailureCategory;
import com.uniai.chat.application.provider.AiProviderStatusSnapshot;

import java.util.Map;

public interface AiProviderStatusPort {
    void recordSuccess(String provider, String model, long latencyMs);

    void recordFailure(String provider, String model, AiProviderFailureCategory failureCategory, long latencyMs);

    AiProviderStatusSnapshot getStatus(String provider);

    Map<String, AiProviderStatusSnapshot> getAllStatuses();
}
