package com.uniai.chat.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Application safety budgets for AI request sizing.
 * These are internal guardrails, not provider quotas.
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.context")
public class AiContextBudgetProperties {

    private int maxInputTokens = 200000;
    private int reservedOutputTokens = 2000;
    private int maxHistoryTokens = 12000;
    private int maxRetrievalTokens = 120000;
    private int charactersPerToken = 4;
    private int requestOverheadTokens = 128;

    private Map<String, ProviderBudget> providers = new HashMap<>();

    @Data
    public static class ProviderBudget {
        private Integer maxInputTokens;
        private Integer reservedOutputTokens;
        private Integer maxHistoryTokens;
        private Integer maxRetrievalTokens;
        private Integer requestOverheadTokens;
    }
}
