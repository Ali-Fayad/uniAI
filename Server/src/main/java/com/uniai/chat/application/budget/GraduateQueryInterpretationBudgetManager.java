package com.uniai.chat.application.budget;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;
import com.uniai.chat.application.memory.ConversationMemory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GraduateQueryInterpretationBudgetManager {

    private static final Logger logger = LogManager.getLogger(GraduateQueryInterpretationBudgetManager.class);
    private static final String BUDGET_EXCEEDED_CATEGORY = "AI_QUERY_INTERPRETATION_BUDGET_EXCEEDED";

    private final GraduateQueryInterpretationBudgetConfiguration configuration;
    private final AiTokenEstimator estimator;
    private final String activeProvider;

    public GraduateQueryInterpretationBudgetManager(
            GraduateQueryInterpretationBudgetConfiguration configuration,
            AiTokenEstimator estimator,
            String activeProvider
    ) {
        this.configuration = configuration;
        this.estimator = estimator;
        this.activeProvider = activeProvider == null ? "placeholder" : activeProvider.trim().toLowerCase();
    }

    public GraduateQueryInterpretationBudgetResult budget(GraduateQueryInterpretationRequest request, String prompt) {
        Objects.requireNonNull(request, "request");
        long startNanos = System.nanoTime();

        List<AiConversationMessage> originalHistory = copyHistory(request.recentConversationHistory());
        List<AiConversationMessage> budgetedHistory = new ArrayList<>(trimToHistoryLimit(originalHistory));

        long maxInputTokens = resolveMaxInputTokens();
        long reservedOutputTokens = resolveReservedOutputTokens();
        long availableInputBudget = Math.max(0L, maxInputTokens - reservedOutputTokens);
        long overheadTokens = estimator.resolveOverheadTokens();

        ConversationMemory conversationMemory = request.conversationMemory();
        long promptTokens = estimator.estimateTokens(prompt);
        long memoryTokens = estimator.estimateTokens(conversationMemory);
        long userTokens = estimator.estimateTokens(request.userMessage());
        long originalHistoryTokens = estimator.estimateConversationTokens(originalHistory);
        long originalTotal = promptTokens + memoryTokens + userTokens + originalHistoryTokens + overheadTokens;

        boolean historyTrimmed = budgetedHistory.size() != originalHistory.size();
        while (!budgetedHistory.isEmpty()
                && (promptTokens + memoryTokens + userTokens + estimator.estimateConversationTokens(budgetedHistory) + overheadTokens) > availableInputBudget) {
            budgetedHistory.remove(0);
            historyTrimmed = true;
        }

        long finalHistoryTokens = estimator.estimateConversationTokens(budgetedHistory);
        long finalEstimatedInputTokens = promptTokens + memoryTokens + userTokens + finalHistoryTokens + overheadTokens;
        boolean requestFits = promptTokens + memoryTokens + userTokens + overheadTokens <= availableInputBudget
                && finalEstimatedInputTokens <= availableInputBudget;

        if (!requestFits) {
            logger.warn("[AI_INTERPRETATION_BUDGET] Request cannot fit provider={} maxInputTokens={} reservedOutputTokens={} finalEstimatedTokens={} category={}",
                    activeProvider,
                    maxInputTokens,
                    reservedOutputTokens,
                    finalEstimatedInputTokens,
                    BUDGET_EXCEEDED_CATEGORY);
        }

        GraduateQueryInterpretationRequest budgetedRequest = new GraduateQueryInterpretationRequest(
                request.userMessage(),
                List.copyOf(budgetedHistory),
                conversationMemory
        );

        logger.debug("[AI_INTERPRETATION_BUDGET] Evaluation completed provider={} originalEstimatedTokens={} finalEstimatedTokens={} promptTokens={} memoryTokens={} userTokens={} historyTokens={} historyTrimmed={} requestFits={} durationMs={}",
                activeProvider,
                originalTotal,
                finalEstimatedInputTokens,
                promptTokens,
                memoryTokens,
                userTokens,
                finalHistoryTokens,
                historyTrimmed,
                requestFits,
                elapsedMillis(startNanos));

        return new GraduateQueryInterpretationBudgetResult(
                budgetedRequest,
                originalTotal,
                finalEstimatedInputTokens,
                maxInputTokens,
                reservedOutputTokens,
                originalHistory.size(),
                budgetedHistory.size(),
                historyTrimmed,
                requestFits,
                requestFits ? null : BUDGET_EXCEEDED_CATEGORY
        );
    }

    private long resolveMaxInputTokens() {
        return configuration != null ? configuration.maxInputTokens() : 1500L;
    }

    private long resolveReservedOutputTokens() {
        return configuration != null ? configuration.maxOutputTokens() : 250L;
    }

    private List<AiConversationMessage> trimToHistoryLimit(List<AiConversationMessage> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        int historyLimit = configuration != null ? configuration.historyMessageLimit() : 4;
        if (historyLimit <= 0) {
            return List.of();
        }
        if (history.size() <= historyLimit) {
            return new ArrayList<>(history);
        }
        return new ArrayList<>(history.subList(history.size() - historyLimit, history.size()));
    }

    private List<AiConversationMessage> copyHistory(List<AiConversationMessage> history) {
        if (history == null || history.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(history);
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
