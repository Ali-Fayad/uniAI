package com.uniai.chat.application.title;

import com.uniai.chat.application.budget.AiTokenEstimator;
import com.uniai.chat.application.dto.ai.AiOperation;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.port.out.ChatTitlePromptPort;
import com.uniai.chat.infrastructure.metrics.ChatAiMetrics;
import com.uniai.chat.domain.repository.ChatRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Locale;
import java.util.concurrent.Executor;

public class ChatTitleGenerationManager {

    private static final Logger logger = LogManager.getLogger(ChatTitleGenerationManager.class);

    private final ChatRepository chatRepository;
    private final AiServicePort aiServicePort;
    private final ChatTitlePromptPort promptPort;
    private final AiTokenEstimator estimator;
    private final ChatTitleGenerationConfiguration configuration;
    private final Executor executor;
    private final String activeProvider;
    private final MeterRegistry meterRegistry;

    public ChatTitleGenerationManager(
            ChatRepository chatRepository,
            AiServicePort aiServicePort,
            ChatTitlePromptPort promptPort,
            AiTokenEstimator estimator,
            ChatTitleGenerationConfiguration configuration,
            Executor executor
    ) {
        this(chatRepository, aiServicePort, promptPort, estimator, configuration, executor, null, null);
    }

    public ChatTitleGenerationManager(
            ChatRepository chatRepository,
            AiServicePort aiServicePort,
            ChatTitlePromptPort promptPort,
            AiTokenEstimator estimator,
            ChatTitleGenerationConfiguration configuration,
            Executor executor,
            String activeProvider
    ) {
        this(chatRepository, aiServicePort, promptPort, estimator, configuration, executor, activeProvider, null);
    }

    public ChatTitleGenerationManager(
            ChatRepository chatRepository,
            AiServicePort aiServicePort,
            ChatTitlePromptPort promptPort,
            AiTokenEstimator estimator,
            ChatTitleGenerationConfiguration configuration,
            Executor executor,
            String activeProvider,
            MeterRegistry meterRegistry
    ) {
        this.chatRepository = chatRepository;
        this.aiServicePort = aiServicePort;
        this.promptPort = promptPort;
        this.estimator = estimator;
        this.configuration = configuration;
        this.executor = executor;
        this.activeProvider = normalizeProvider(activeProvider);
        this.meterRegistry = meterRegistry;
    }

    public void generateTitleIfNeeded(Long chatId, String firstUserMessage) {
        if (!isEnabled() || configuration == null || chatId == null || !hasText(firstUserMessage) || executor == null || isPlaceholderProvider()) {
            return;
        }

        try {
            executor.execute(() -> generateTitle(chatId, firstUserMessage));
        } catch (RuntimeException ex) {
            logger.warn("[CHAT_TITLE] Title generation scheduling failed chatId={} reason={}", chatId, ex.getMessage());
        }
    }

    private void generateTitle(Long chatId, String firstUserMessage) {
        try {
            String existingTitle = chatRepository != null ? chatRepository.findTitleById(chatId) : null;
            if (hasText(existingTitle)) {
                logger.debug("[CHAT_TITLE] Title already exists chatId={}", chatId);
                return;
            }

            String prompt = promptPort != null ? promptPort.getPrompt() : "";
            if (!budgetFits(prompt, firstUserMessage)) {
                logger.warn("[CHAT_TITLE] Title budget rejected chatId={} category=CHAT_TITLE_BUDGET_EXCEEDED", chatId);
                ChatAiMetrics.incrementCounter(
                        meterRegistry,
                        ChatAiMetrics.BUDGET_REJECTIONS,
                        "Budget rejections for AI requests",
                        "operation",
                        "title_generation",
                        "provider",
                        hasText(activeProvider) ? activeProvider : "unknown"
                );
                return;
            }

            AiRequest request = AiRequest.builder()
                    .systemPrompt(prompt)
                    .userMessage(firstUserMessage)
                    .operation(AiOperation.TITLE_GENERATION)
                    .temperature(0.0)
                    .maxTokens(configuration.maxOutputTokens())
                    .build();

            AiResponse response = aiServicePort == null ? null : aiServicePort.generateResponse(request);
            if (response == null || Boolean.TRUE.equals(response.getFallback())) {
                logger.warn("[CHAT_TITLE] Title provider failed chatId={} reason=fallback-or-null", chatId);
                return;
            }

            String normalizedTitle = normalizeTitle(response.getContent());
            if (!hasText(normalizedTitle)) {
                logger.warn("[CHAT_TITLE] Title validation failed chatId={} reason=invalid-output", chatId);
                return;
            }

            boolean saved = chatRepository != null && chatRepository.updateTitleIfAbsent(chatId, normalizedTitle);
            if (!saved) {
                logger.debug("[CHAT_TITLE] Title persistence skipped chatId={} reason=already-set-or-missing", chatId);
                return;
            }

            logger.info("[CHAT_TITLE] Title generated chatId={} titleLength={}", chatId, normalizedTitle.length());
        } catch (RuntimeException ex) {
            logger.warn("[CHAT_TITLE] Title generation failed chatId={} reason={}", chatId, ex.getMessage());
        }
    }

    private boolean budgetFits(String prompt, String firstUserMessage) {
        long promptTokens = estimator != null ? estimator.estimateTokens(prompt) : 0L;
        long messageTokens = estimator != null ? estimator.estimateTokens(firstUserMessage) : 0L;
        long overheadTokens = estimator != null ? estimator.resolveOverheadTokens() : 128L;
        long reservedOutputTokens = configuration.maxOutputTokens();
        long total = promptTokens + messageTokens + overheadTokens;
        long availableInputBudget = configuration.maxInputTokens() - reservedOutputTokens;
        return total <= Math.max(0L, availableInputBudget);
    }

    private String normalizeTitle(String rawTitle) {
        if (!hasText(rawTitle)) {
            return null;
        }

        String title = rawTitle.trim();
        if (title.startsWith("```") || title.contains("\n") || title.contains("\r")) {
            return null;
        }

        title = stripSurroundingQuotes(title);
        title = title.replaceAll("\\s+", " ").trim();
        title = stripTrailingPunctuation(title);

        if (!hasText(title) || title.length() > configuration.maxTitleLength()) {
            return null;
        }

        if (containsMarkdownMarkers(title)) {
            return null;
        }

        if (isSuspiciousOutput(title)) {
            return null;
        }

        int wordCount = title.split("\\s+").length;
        if (wordCount > 6) {
            return null;
        }

        return title;
    }

    private String stripSurroundingQuotes(String title) {
        if (title.length() < 2) {
            return title;
        }

        char first = title.charAt(0);
        char last = title.charAt(title.length() - 1);
        if ((first == '"' && last == '"')
                || (first == '\'' && last == '\'')
                || (first == '“' && last == '”')
                || (first == '‘' && last == '’')) {
            return title.substring(1, title.length() - 1).trim();
        }
        return title;
    }

    private String stripTrailingPunctuation(String title) {
        String result = title;
        while (!result.isEmpty()) {
            char last = result.charAt(result.length() - 1);
            if (last == '.' || last == '!' || last == '?' || last == ':' || last == ';' || last == ',') {
                result = result.substring(0, result.length() - 1).trim();
                continue;
            }
            break;
        }
        return result;
    }

    private boolean containsMarkdownMarkers(String title) {
        String lower = title.toLowerCase(Locale.ROOT);
        return lower.startsWith("- ")
                || lower.startsWith("* ")
                || lower.startsWith("# ")
                || title.contains("```")
                || title.contains("`")
                || title.contains("*")
                || title.contains("_")
                || title.contains("[")
                || title.contains("]");
    }

    private boolean isSuspiciousOutput(String title) {
        String lower = title.toLowerCase(Locale.ROOT);
        return lower.startsWith("ai response to")
                || lower.startsWith("here is")
                || lower.startsWith("title:")
                || lower.startsWith("summary:")
                || lower.startsWith("response:");
    }

    private boolean isEnabled() {
        return configuration != null && configuration.enabled();
    }

    private boolean isPlaceholderProvider() {
        return "placeholder".equals(activeProvider);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeProvider(String provider) {
        return provider == null ? null : provider.trim().toLowerCase(Locale.ROOT);
    }
}
