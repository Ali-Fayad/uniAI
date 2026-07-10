package com.uniai.chat.application.budget;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.dto.ai.AiRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AiContextBudgetManager {

    private static final Logger logger = LogManager.getLogger(AiContextBudgetManager.class);
    private static final String BUDGET_EXCEEDED_CATEGORY = "AI_CONTEXT_BUDGET_EXCEEDED";
    private static final String CONTEXT_TRUNCATION_MARKER = "[Retrieved context truncated to fit the configured AI request budget.]";

    private final AiContextBudgetConfiguration configuration;
    private final AiTokenEstimator estimator;
    private final String activeProvider;

    public AiContextBudgetManager(
            AiContextBudgetConfiguration configuration,
            AiTokenEstimator estimator,
            String activeProvider
    ) {
        this.configuration = configuration;
        this.estimator = estimator;
        this.activeProvider = normalizeProvider(activeProvider);
    }

    public AiContextBudgetResult budget(AiRequest request) {
        Objects.requireNonNull(request, "request");
        long startNanos = System.nanoTime();

        long maxInputTokens = resolveMaxInputTokens();
        long reservedOutputTokens = resolveReservedOutputTokens(request);
        long availableInputBudget = Math.max(0L, maxInputTokens - reservedOutputTokens);
        long overheadTokens = resolveRequestOverheadTokens();

        List<AiConversationMessage> originalHistory = copyHistory(request.getConversationHistory());
        List<String> originalContext = copyContext(request.getContext());

        long originalSystemTokens = estimator.estimateTokens(request.getSystemPrompt());
        long originalUserTokens = estimator.estimateTokens(request.getUserMessage());
        long originalHistoryTokens = estimator.estimateConversationTokens(originalHistory);
        long originalContextTokens = estimator.estimateContextTokens(originalContext);
        long originalTotal = originalSystemTokens + originalUserTokens + originalHistoryTokens + originalContextTokens + overheadTokens;

        logger.debug("[AI_BUDGET] Evaluation started provider={} maxInputTokens={} reservedOutputTokens={} overheadTokens={}",
                activeProvider,
                maxInputTokens,
                reservedOutputTokens,
                overheadTokens);
        logger.debug("[AI_BUDGET] Original estimate total={} system={} history={} retrieval={} user={} contextCount={} historyCount={}",
                originalTotal,
                originalSystemTokens,
                originalHistoryTokens,
                originalContextTokens,
                originalUserTokens,
                originalContext.size(),
                originalHistory.size());

        List<AiConversationMessage> budgetedHistory = new ArrayList<>(originalHistory);
        List<String> budgetedContext = new ArrayList<>(originalContext);
        boolean historyTrimmed = false;
        boolean contextTrimmed = false;

        TrimState historyTrimState = trimHistoryToBudget(budgetedHistory, resolveHistoryBudgetTokens(), availableInputBudget, request, budgetedContext);
        historyTrimmed |= historyTrimState.trimmed();

        ContextTrimState contextTrimState = trimContextToBudget(budgetedContext, resolveRetrievalBudgetTokens(), availableInputBudget, request, budgetedHistory);
        contextTrimmed |= contextTrimState.trimmed();

        long finalHistoryTokens = estimator.estimateConversationTokens(budgetedHistory);
        long finalContextTokens = estimator.estimateContextTokens(budgetedContext);
        long finalSystemTokens = estimator.estimateTokens(request.getSystemPrompt());
        long finalUserTokens = estimator.estimateTokens(request.getUserMessage());
        long finalTotal = finalSystemTokens + finalUserTokens + finalHistoryTokens + finalContextTokens + overheadTokens;

        if (finalTotal > availableInputBudget) {
            TrimState overallHistoryTrimState = trimHistoryToFitOverallBudget(
                    budgetedHistory,
                    budgetedContext,
                    availableInputBudget,
                    request
            );
            historyTrimmed |= overallHistoryTrimState.trimmed();

            ContextTrimState overallContextTrimState = trimContextToFitOverallBudget(
                    budgetedContext,
                    budgetedHistory,
                    availableInputBudget,
                    request
            );
            contextTrimmed |= overallContextTrimState.trimmed();

            finalHistoryTokens = estimator.estimateConversationTokens(budgetedHistory);
            finalContextTokens = estimator.estimateContextTokens(budgetedContext);
            finalTotal = finalSystemTokens + finalUserTokens + finalHistoryTokens + finalContextTokens + overheadTokens;
        }

        long finalEstimatedInputTokens = finalTotal;
        boolean requestFits = finalEstimatedInputTokens <= availableInputBudget
                && finalSystemTokens + finalUserTokens + overheadTokens <= availableInputBudget;

        if (historyTrimmed) {
            logger.warn("[AI_BUDGET] History trimmed originalCount={} finalCount={} estimatedTokensRemoved={}",
                    originalHistory.size(),
                    budgetedHistory.size(),
                    Math.max(0L, originalHistoryTokens - finalHistoryTokens));
        }
        if (contextTrimmed) {
            logger.warn("[AI_BUDGET] Retrieval trimmed originalCount={} finalCount={} originalTokens={} finalTokens={}",
                    originalContext.size(),
                    budgetedContext.size(),
                    originalContextTokens,
                    finalContextTokens);
        }
        if (!requestFits) {
            logger.warn("[AI_BUDGET] Request cannot fit provider={} maxInputTokens={} reservedOutputTokens={} finalEstimatedTokens={} category={}",
                    activeProvider,
                    maxInputTokens,
                    reservedOutputTokens,
                    finalEstimatedInputTokens,
                    BUDGET_EXCEEDED_CATEGORY);
        }

        AiRequest budgetedRequest = AiRequest.builder()
                .userMessage(request.getUserMessage())
                .systemPrompt(request.getSystemPrompt())
                .conversationHistory(List.copyOf(budgetedHistory))
                .context(List.copyOf(budgetedContext))
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens() != null && request.getMaxTokens() > 0
                        ? request.getMaxTokens()
                        : (int) reservedOutputTokens)
                .build();

        logger.debug("[AI_BUDGET] Evaluation completed finalEstimatedTokens={} requestFits={} durationMs={} historyTrimmed={} contextTrimmed={} finalHistoryCount={} finalContextCount={}",
                finalEstimatedInputTokens,
                requestFits,
                elapsedMillis(startNanos),
                historyTrimmed,
                contextTrimmed,
                budgetedHistory.size(),
                budgetedContext.size());

        return new AiContextBudgetResult(
                budgetedRequest,
                originalTotal,
                finalEstimatedInputTokens,
                maxInputTokens,
                reservedOutputTokens,
                originalHistory.size(),
                budgetedHistory.size(),
                originalContext.size(),
                budgetedContext.size(),
                historyTrimmed,
                contextTrimmed,
                requestFits,
                activeProvider,
                requestFits ? null : BUDGET_EXCEEDED_CATEGORY
        );
    }

    private TrimState trimHistoryToBudget(
            List<AiConversationMessage> history,
            long historyBudgetTokens,
            long availableInputBudget,
            AiRequest request,
            List<String> context
    ) {
        if (history.isEmpty()) {
            return TrimState.ofNotTrimmed();
        }

        long historyTokens = estimator.estimateConversationTokens(history);
        boolean trimmed = false;
        while (!history.isEmpty() && historyTokens > historyBudgetTokens) {
            historyTokens = removeOldestHistoryMessage(history, historyTokens);
            trimmed = true;
        }

        return trimmed ? TrimState.ofTrimmed() : TrimState.ofNotTrimmed();
    }

    private TrimState trimHistoryToFitOverallBudget(
            List<AiConversationMessage> history,
            List<String> context,
            long availableInputBudget,
            AiRequest request
    ) {
        if (history.isEmpty()) {
            return TrimState.ofNotTrimmed();
        }

        boolean trimmed = false;
        while (!history.isEmpty() && estimateTotalTokens(request, history, context) > availableInputBudget) {
            long before = estimator.estimateConversationTokens(history);
            removeOldestHistoryMessage(history, before);
            trimmed = true;
        }
        return trimmed ? TrimState.ofTrimmed() : TrimState.ofNotTrimmed();
    }

    private ContextTrimState trimContextToBudget(
            List<String> context,
            long retrievalBudgetTokens,
            long availableInputBudget,
            AiRequest request,
            List<AiConversationMessage> history
    ) {
        if (context.isEmpty()) {
            return ContextTrimState.ofNotTrimmed();
        }

        boolean trimmed = false;
        boolean removedBlankEntries = sanitizeContext(context);
        long contextTokens = estimator.estimateContextTokens(context);

        while (context.size() > 1 && contextTokens > retrievalBudgetTokens) {
            String removed = context.remove(context.size() - 1);
            contextTokens -= estimator.estimateTokens(removed);
            trimmed = true;
        }

        if (contextTokens > retrievalBudgetTokens && !context.isEmpty()) {
            String truncated = truncateContextForBudget(context.get(0), retrievalBudgetTokens);
            if (!truncated.equals(context.get(0))) {
                context.set(0, truncated);
                contextTokens = estimator.estimateContextTokens(context);
                trimmed = true;
            }
        }

        if (trimmed) {
            appendTruncationMarker(context);
        }

        return (trimmed || removedBlankEntries) ? ContextTrimState.ofTrimmed() : ContextTrimState.ofNotTrimmed();
    }

    private ContextTrimState trimContextToFitOverallBudget(
            List<String> context,
            List<AiConversationMessage> history,
            long availableInputBudget,
            AiRequest request
    ) {
        if (context.isEmpty()) {
            return ContextTrimState.ofNotTrimmed();
        }

        boolean trimmed = false;
        while (!context.isEmpty() && estimateTotalTokens(request, history, context) > availableInputBudget) {
            if (context.size() > 1) {
                context.remove(context.size() - 1);
                trimmed = true;
                continue;
            }

            long remainingBudgetTokens = estimateRemainingContextBudget(request, history, context, availableInputBudget);
            if (remainingBudgetTokens <= 0) {
                context.clear();
                trimmed = true;
                break;
            }

            String truncated = truncateContextForBudget(context.get(0), remainingBudgetTokens);
            if (!truncated.equals(context.get(0))) {
                context.set(0, truncated);
                trimmed = true;
                appendTruncationMarker(context);
                if (context.size() == 1 && CONTEXT_TRUNCATION_MARKER.equals(context.get(0))
                        && estimateTotalTokens(request, history, context) > availableInputBudget) {
                    context.clear();
                    break;
                }
            } else {
                break;
            }
        }
        return trimmed ? ContextTrimState.ofTrimmed() : ContextTrimState.ofNotTrimmed();
    }

    private long estimateTotalTokens(AiRequest request, List<AiConversationMessage> history, List<String> context) {
        return estimator.estimateTokens(request.getSystemPrompt())
                + estimator.estimateTokens(request.getUserMessage())
                + estimator.estimateConversationTokens(history)
                + estimator.estimateContextTokens(context)
                + resolveRequestOverheadTokens();
    }

    private long removeOldestHistoryMessage(List<AiConversationMessage> history, long currentTokens) {
        if (history.isEmpty()) {
            return currentTokens;
        }

        AiConversationMessage removed = history.remove(0);
        currentTokens -= estimator.estimateTokens(removed);

        while (!history.isEmpty() && isAssistant(history.get(0))) {
            AiConversationMessage orphanAssistant = history.remove(0);
            currentTokens -= estimator.estimateTokens(orphanAssistant);
        }

        return currentTokens;
    }

    private boolean isAssistant(AiConversationMessage message) {
        if (message == null || !hasText(message.getRole())) {
            return false;
        }
        String normalized = message.getRole().trim().toLowerCase(Locale.ROOT);
        return "assistant".equals(normalized) || "model".equals(normalized);
    }

    private long estimateRemainingContextBudget(
            AiRequest request,
            List<AiConversationMessage> history,
            List<String> context,
            long availableInputBudget
    ) {
        long mandatory = estimator.estimateTokens(request.getSystemPrompt())
                + estimator.estimateTokens(request.getUserMessage())
                + estimator.estimateConversationTokens(history)
                + resolveRequestOverheadTokens();
        return Math.max(0L, availableInputBudget - mandatory);
    }

    private long resolveMaxInputTokens() {
        int globalMax = Math.max(1, configuration != null ? configuration.maxInputTokens() : 200000);
        AiContextBudgetConfiguration.ProviderBudget providerBudget = resolveProviderBudget();
        if (providerBudget != null && providerBudget.maxInputTokens() != null) {
            return Math.max(1, providerBudget.maxInputTokens());
        }
        return globalMax;
    }

    private long resolveReservedOutputTokens(AiRequest request) {
        if (request != null && request.getMaxTokens() != null && request.getMaxTokens() > 0) {
            return request.getMaxTokens();
        }
        AiContextBudgetConfiguration.ProviderBudget providerBudget = resolveProviderBudget();
        if (providerBudget != null && providerBudget.reservedOutputTokens() != null) {
            return Math.max(0, providerBudget.reservedOutputTokens());
        }
        return Math.max(0, configuration != null ? configuration.reservedOutputTokens() : 2000);
    }

    private long resolveHistoryBudgetTokens() {
        AiContextBudgetConfiguration.ProviderBudget providerBudget = resolveProviderBudget();
        if (providerBudget != null && providerBudget.maxHistoryTokens() != null) {
            return Math.max(0, providerBudget.maxHistoryTokens());
        }
        return Math.max(0, configuration != null ? configuration.maxHistoryTokens() : 12000);
    }

    private long resolveRetrievalBudgetTokens() {
        AiContextBudgetConfiguration.ProviderBudget providerBudget = resolveProviderBudget();
        if (providerBudget != null && providerBudget.maxRetrievalTokens() != null) {
            return Math.max(0, providerBudget.maxRetrievalTokens());
        }
        return Math.max(0, configuration != null ? configuration.maxRetrievalTokens() : 120000);
    }

    private long resolveRequestOverheadTokens() {
        AiContextBudgetConfiguration.ProviderBudget providerBudget = resolveProviderBudget();
        if (providerBudget != null && providerBudget.requestOverheadTokens() != null) {
            return Math.max(0, providerBudget.requestOverheadTokens());
        }
        return estimator.resolveOverheadTokens();
    }

    private AiContextBudgetConfiguration.ProviderBudget resolveProviderBudget() {
        if (configuration == null || configuration.providers() == null || configuration.providers().isEmpty()) {
            return null;
        }
        return configuration.providerBudget(activeProvider);
    }

    private List<AiConversationMessage> copyHistory(List<AiConversationMessage> history) {
        if (history == null || history.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(history);
    }

    private List<String> copyContext(List<String> context) {
        if (context == null || context.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(context);
    }

    private boolean sanitizeContext(List<String> context) {
        if (context == null || context.isEmpty()) {
            return false;
        }
        boolean removed = context.removeIf(entry -> !hasText(entry));
        return removed;
    }

    private String truncateContextForBudget(String contextEntry, long remainingBudgetTokens) {
        if (!hasText(contextEntry)) {
            return "";
        }

        long safeBudgetTokens = Math.max(0L, remainingBudgetTokens);
        int charsPerToken = (int) Math.max(1L, estimator.resolveCharactersPerToken());
        int allowedChars = (int) Math.max(0L, safeBudgetTokens * (long) charsPerToken);
        if (contextEntry.length() <= allowedChars) {
            return contextEntry;
        }

        String prefix = contextEntry.substring(0, allowedChars).stripTrailing();
        if (!hasText(prefix)) {
            return "";
        }
        return prefix;
    }

    private void appendTruncationMarker(List<String> context) {
        if (context.isEmpty()) {
            context.add(CONTEXT_TRUNCATION_MARKER);
            return;
        }

        String lastEntry = context.get(context.size() - 1);
        if (CONTEXT_TRUNCATION_MARKER.equals(lastEntry)) {
            return;
        }
        if (!hasText(lastEntry) && context.size() == 1) {
            context.set(0, CONTEXT_TRUNCATION_MARKER);
            return;
        }
        context.add(CONTEXT_TRUNCATION_MARKER);
    }

    private String normalizeProvider(String provider) {
        return !hasText(provider) ? "placeholder" : provider.trim().toLowerCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private record TrimState(boolean trimmed) {
        private static TrimState ofTrimmed() {
            return new TrimState(true);
        }

        private static TrimState ofNotTrimmed() {
            return new TrimState(false);
        }
    }

    private record ContextTrimState(boolean trimmed) {
        private static ContextTrimState ofTrimmed() {
            return new ContextTrimState(true);
        }

        private static ContextTrimState ofNotTrimmed() {
            return new ContextTrimState(false);
        }
    }
}
