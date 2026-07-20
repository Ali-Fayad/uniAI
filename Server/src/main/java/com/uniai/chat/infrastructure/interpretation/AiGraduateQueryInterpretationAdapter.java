package com.uniai.chat.infrastructure.interpretation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.dto.ai.AiOperation;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.ConversationMemoryPromptFormatter;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretation;
import com.uniai.chat.application.interpretation.CompactGraduateQueryInterpretation;
import com.uniai.chat.application.interpretation.CanonicalGraduateQueryDraft;
import com.uniai.chat.application.interpretation.CanonicalGraduateQueryDraftCompatibility;
import com.uniai.chat.application.interpretation.CanonicalGraduateQueryDraftValidator;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationProviderException;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.port.out.GraduateQueryInterpretationPort;
import com.uniai.chat.application.port.out.CanonicalGraduateQueryDraftPort;
import com.uniai.chat.application.port.out.GraduateQueryInterpreterPromptPort;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetConfiguration;
import com.uniai.chat.infrastructure.metrics.ChatAiMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.util.StringUtils;

import java.util.List;

public class AiGraduateQueryInterpretationAdapter implements GraduateQueryInterpretationPort, CanonicalGraduateQueryDraftPort {

    private static final Logger logger = LogManager.getLogger(AiGraduateQueryInterpretationAdapter.class);
    private static final String FALLBACK_MESSAGE = "AI service returned a fallback response for graduate query interpretation.";

    private final AiServicePort aiServicePort;
    private final GraduateQueryInterpreterPromptPort promptPort;
    private final GraduateQueryInterpretationBudgetConfiguration budgetConfiguration;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final CanonicalGraduateQueryDraftValidator draftValidator;

    public AiGraduateQueryInterpretationAdapter(
            AiServicePort aiServicePort,
            GraduateQueryInterpreterPromptPort promptPort,
            GraduateQueryInterpretationBudgetConfiguration budgetConfiguration,
            ObjectMapper objectMapper
    ) {
        this(aiServicePort, promptPort, budgetConfiguration, objectMapper, null);
    }

    public AiGraduateQueryInterpretationAdapter(
            AiServicePort aiServicePort,
            GraduateQueryInterpreterPromptPort promptPort,
            GraduateQueryInterpretationBudgetConfiguration budgetConfiguration,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry
    ) {
        this.aiServicePort = aiServicePort;
        this.promptPort = promptPort;
        this.budgetConfiguration = budgetConfiguration;
        this.objectMapper = objectMapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.meterRegistry = meterRegistry;
        this.draftValidator = new CanonicalGraduateQueryDraftValidator();
    }

    @Override
    public GraduateQueryInterpretation interpret(GraduateQueryInterpretationRequest request) {
        AiResponse aiResponse = requestProvider(request);
        String cleaned = cleanResponse(aiResponse);
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(cleaned);
            if (root.has("intent")) {
                // Temporary compatibility input for callers/providers still on the pre-draft contract.
                return objectMapper.treeToValue(root, GraduateQueryInterpretation.class);
            }
            if (root.path("schemaVersion").asInt() == 1) {
                // Temporary compatibility input for the Task 1 compact contract.
                return objectMapper.treeToValue(root, CompactGraduateQueryInterpretation.class).toLegacyInterpretation();
            }
            CanonicalGraduateQueryDraft draft = draftValidator.validate(
                    objectMapper.treeToValue(root, CanonicalGraduateQueryDraft.class));
            return CanonicalGraduateQueryDraftCompatibility.toLegacyInterpretation(draft);
        } catch (Exception ex) {
            recordParseFailure(aiResponse, ex);
            throw new GraduateQueryInterpretationProviderException(
                    "Failed to parse graduate query interpretation JSON",
                    "AI_QUERY_INTERPRETATION_PROVIDER_INVALID",
                    ex);
        }
    }

    @Override
    public CanonicalGraduateQueryDraft interpretDraft(GraduateQueryInterpretationRequest request) {
        AiResponse aiResponse = requestProvider(request);
        String cleaned = cleanResponse(aiResponse);
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(cleaned);
            if (root.has("intent")) {
                throw new IllegalStateException("Legacy interpretation contract is not a canonical draft");
            }
            return draftValidator.validate(objectMapper.treeToValue(root, CanonicalGraduateQueryDraft.class));
        } catch (Exception ex) {
            recordParseFailure(aiResponse, ex);
            throw new GraduateQueryInterpretationProviderException(
                    "Failed to parse canonical graduate query draft JSON",
                    "AI_QUERY_INTERPRETATION_PROVIDER_INVALID",
                    ex);
        }
    }

    private AiResponse requestProvider(GraduateQueryInterpretationRequest request) {
        long startNanos = System.nanoTime();
        String prompt = promptPort.getPrompt();
        String userMessage = request != null ? request.userMessage() : null;
        List<AiConversationMessage> history = request != null ? request.recentConversationHistory() : List.of();
        ConversationMemory conversationMemory = request != null ? request.conversationMemory() : ConversationMemory.empty();
        String systemPrompt = appendMemory(prompt, conversationMemory);

        logger.debug("[AI_INTERPRETATION] Request started providerBean={} promptLength={} memoryLength={} messageLength={} historyCount={} maxTokens={}",
                aiServicePort.getClass().getSimpleName(),
                StringUtils.hasText(prompt) ? prompt.length() : 0,
                ConversationMemoryPromptFormatter.render(conversationMemory).length(),
                StringUtils.hasText(userMessage) ? userMessage.length() : 0,
                history.size(),
                budgetConfiguration != null ? budgetConfiguration.maxOutputTokens() : 0);

        AiRequest aiRequest = AiRequest.builder()
                .systemPrompt(systemPrompt)
                .userMessage(userMessage)
                .conversationHistory(history)
                .conversationMemory(conversationMemory)
                .operation(AiOperation.INTERPRETATION)
                .temperature(0.0)
                .maxTokens(budgetConfiguration != null ? budgetConfiguration.maxOutputTokens() : 250)
                .build();

        AiResponse aiResponse = aiServicePort.generateResponse(aiRequest);
        if (aiResponse == null) {
            throw new GraduateQueryInterpretationProviderException(
                    "Interpretation provider returned null response",
                    "AI_QUERY_INTERPRETATION_PROVIDER_UNAVAILABLE");
        }
        if (Boolean.TRUE.equals(aiResponse.getFallback())) {
            logger.warn("[AI_INTERPRETATION] Provider fallback received provider={} model={} durationMs={}",
                    aiResponse.getProvider(),
                    aiResponse.getModel(),
                    elapsedMillis(startNanos));
            throw new GraduateQueryInterpretationProviderException(
                    "Interpretation provider fallback response",
                    "AI_QUERY_INTERPRETATION_PROVIDER_UNAVAILABLE");
        }
        if ("MAX_TOKENS".equalsIgnoreCase(aiResponse.getFinishReason())
                || "LENGTH".equalsIgnoreCase(aiResponse.getFinishReason())) {
            logger.warn("[AI_INTERPRETATION] Provider response truncated provider={} model={} durationMs={}",
                    aiResponse.getProvider(), aiResponse.getModel(), elapsedMillis(startNanos));
            throw new GraduateQueryInterpretationProviderException(
                    "Interpretation provider response reached max output tokens",
                    "AI_QUERY_INTERPRETATION_PROVIDER_TRUNCATED");
        }

        logger.debug("[AI_INTERPRETATION] Provider response received provider={} model={} durationMs={}",
                aiResponse.getProvider(), aiResponse.getModel(), elapsedMillis(startNanos));
        return aiResponse;
    }

    private String cleanResponse(AiResponse aiResponse) {
        String content = aiResponse.getContent();
        if (!StringUtils.hasText(content)) {
            logger.warn("[AI_INTERPRETATION] Empty response received provider={} model={}",
                    aiResponse.getProvider(),
                    aiResponse.getModel());
            throw new GraduateQueryInterpretationProviderException(
                    "Interpretation provider returned empty content",
                    "AI_QUERY_INTERPRETATION_PROVIDER_EMPTY");
        }
        return stripJsonFences(content.trim());
    }

    private void recordParseFailure(AiResponse aiResponse, Exception ex) {
        logger.warn("[AI_INTERPRETATION] Failed to parse canonical interpretation JSON provider={} model={} reason={}",
                aiResponse.getProvider(), aiResponse.getModel(), ex.getMessage());
            ChatAiMetrics.incrementCounter(
                    meterRegistry,
                    ChatAiMetrics.INTERPRETATION_INVALID,
                    "Structured-output interpretation failures",
                    "provider",
                    ChatAiMetrics.normalizeTagValue(aiResponse.getProvider()),
                    "model",
                    ChatAiMetrics.normalizeTagValue(aiResponse.getModel()),
                    "reason",
                    "malformed_json"
            );
    }

    private String stripJsonFences(String content) {
        if (!StringUtils.hasText(content)) {
            return content;
        }

        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline >= 0 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    private String appendMemory(String prompt, ConversationMemory conversationMemory) {
        if (conversationMemory == null || conversationMemory.isEmpty()) {
            return prompt;
        }
        String memoryText = ConversationMemoryPromptFormatter.render(conversationMemory);
        if (!StringUtils.hasText(memoryText)) {
            return prompt;
        }
        if (!StringUtils.hasText(prompt)) {
            return "Trusted conversation memory:\n" + memoryText;
        }
        return prompt + "\n\nTrusted conversation memory:\n" + memoryText;
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
