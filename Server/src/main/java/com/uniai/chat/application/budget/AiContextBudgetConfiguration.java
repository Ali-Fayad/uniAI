package com.uniai.chat.application.budget;

import java.util.Map;
import java.util.Locale;

/**
 * Application-owned budget policy for request sizing.
 */
public record AiContextBudgetConfiguration(
        int maxInputTokens,
        int reservedOutputTokens,
        int maxHistoryTokens,
        int maxRetrievalTokens,
        int charactersPerToken,
        int requestOverheadTokens,
        Map<String, ProviderBudget> providers
) {
    public AiContextBudgetConfiguration {
        providers = providers == null ? Map.of() : Map.copyOf(providers);
        maxInputTokens = Math.max(1, maxInputTokens);
        reservedOutputTokens = Math.max(0, reservedOutputTokens);
        maxHistoryTokens = Math.max(0, maxHistoryTokens);
        maxRetrievalTokens = Math.max(0, maxRetrievalTokens);
        charactersPerToken = Math.max(1, charactersPerToken);
        requestOverheadTokens = Math.max(0, requestOverheadTokens);
    }

    public ProviderBudget providerBudget(String provider) {
        if (provider == null || providers.isEmpty() || provider.isBlank()) {
            return null;
        }
        return providers.get(provider.trim().toLowerCase(Locale.ROOT));
    }

    public record ProviderBudget(
            Integer maxInputTokens,
            Integer reservedOutputTokens,
            Integer maxHistoryTokens,
            Integer maxRetrievalTokens,
            Integer requestOverheadTokens
    ) {
    }
}
