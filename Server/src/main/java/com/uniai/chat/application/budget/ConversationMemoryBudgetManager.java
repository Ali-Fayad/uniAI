package com.uniai.chat.application.budget;

import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.ConversationMemoryPromptFormatter;
import com.uniai.chat.application.memory.ConversationMemoryUpdateRequest;
import com.uniai.chat.infrastructure.metrics.ChatAiMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.micrometer.core.instrument.MeterRegistry;

public class ConversationMemoryBudgetManager {

    private static final Logger logger = LogManager.getLogger(ConversationMemoryBudgetManager.class);
    private final ConversationMemoryBudgetConfiguration configuration;
    private final AiTokenEstimator estimator;
    private final String provider;
    private final MeterRegistry meterRegistry;

    public ConversationMemoryBudgetManager(
            ConversationMemoryBudgetConfiguration configuration,
            AiTokenEstimator estimator,
            String provider
    ) {
        this(configuration, estimator, provider, null);
    }

    public ConversationMemoryBudgetManager(
            ConversationMemoryBudgetConfiguration configuration,
            AiTokenEstimator estimator,
            String provider,
            MeterRegistry meterRegistry
    ) {
        this.configuration = configuration;
        this.estimator = estimator;
        this.provider = provider == null ? "placeholder" : provider.trim().toLowerCase();
        this.meterRegistry = meterRegistry;
    }

    public ConversationMemoryBudgetResult budget(ConversationMemoryUpdateRequest request, String prompt) {
        long startedNanos = System.nanoTime();
        if (request == null || configuration == null || !configuration.enabled()) {
            return new ConversationMemoryBudgetResult(request, 0L, 0L, 0L, 0L, false, "AI_MEMORY_DISABLED");
        }

        String memoryText = ConversationMemoryPromptFormatter.render(request.previousMemory());
        long promptTokens = estimator.estimateTokens(prompt);
        long memoryTokens = estimator.estimateTokens(memoryText);
        long userTokens = estimator.estimateTokens(request.currentUserMessage());
        long assistantTokens = estimator.estimateTokens(request.assistantResponse());
        long interpretationTokens = estimator.estimateTokens(renderInterpretationSummary(request));
        long overheadTokens = estimator.resolveOverheadTokens();
        long original = promptTokens + memoryTokens + userTokens + assistantTokens + interpretationTokens + overheadTokens;
        long maxInputTokens = configuration.maxInputTokens();
        long reservedOutputTokens = configuration.maxOutputTokens();
        boolean requestFits = original + reservedOutputTokens <= maxInputTokens;
        if (!requestFits) {
            ChatAiMetrics.incrementCounter(
                    meterRegistry,
                    ChatAiMetrics.BUDGET_REJECTIONS,
                    "Budget rejections for AI requests",
                    "operation",
                    "memory_update",
                    "provider",
                    provider
            );
        }
        logger.debug("[AI_MEMORY_BUDGET] Evaluation completed provider={} requestFits={} originalEstimatedTokens={} maxInputTokens={} reservedOutputTokens={} durationMs={}",
                provider,
                requestFits,
                original,
                maxInputTokens,
                reservedOutputTokens,
                elapsedMillis(startedNanos));
        return new ConversationMemoryBudgetResult(request, original, original, maxInputTokens, reservedOutputTokens, requestFits,
                requestFits ? "" : "AI_MEMORY_BUDGET_EXCEEDED");
    }

    private String renderInterpretationSummary(ConversationMemoryUpdateRequest request) {
        if (request.interpretationResult() == null) {
            return "";
        }
        return "status=" + request.interpretationResult().status()
                + "\nresolvedUniversities=" + request.interpretationResult().resolvedUniversityCount()
                + "\ndegreeTypes=" + request.interpretationResult().degreeTypeCount()
                + "\nambiguous=" + request.interpretationResult().ambiguous()
                + "\nfallbackUsed=" + request.interpretationResult().fallbackUsed()
                + "\nfailureCategory=" + request.interpretationResult().failureCategory()
                + "\nunsupportedConstraints=" + request.interpretationResult().unsupportedConstraints();
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
