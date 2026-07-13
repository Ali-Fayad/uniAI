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
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.port.out.GraduateQueryInterpretationPort;
import com.uniai.chat.application.port.out.GraduateQueryInterpreterPromptPort;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetConfiguration;
import com.uniai.chat.infrastructure.metrics.ChatAiMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.util.StringUtils;

import java.util.List;

public class AiGraduateQueryInterpretationAdapter implements GraduateQueryInterpretationPort {

    private static final Logger logger = LogManager.getLogger(AiGraduateQueryInterpretationAdapter.class);
    private static final String FALLBACK_MESSAGE = "AI service returned a fallback response for graduate query interpretation.";

    private final AiServicePort aiServicePort;
    private final GraduateQueryInterpreterPromptPort promptPort;
    private final GraduateQueryInterpretationBudgetConfiguration budgetConfiguration;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

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
    }

    @Override
    public GraduateQueryInterpretation interpret(GraduateQueryInterpretationRequest request) {
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
            throw new IllegalStateException("Interpretation provider returned null response");
        }
        if (Boolean.TRUE.equals(aiResponse.getFallback())) {
            logger.warn("[AI_INTERPRETATION] Provider fallback received provider={} model={} durationMs={}",
                    aiResponse.getProvider(),
                    aiResponse.getModel(),
                    elapsedMillis(startNanos));
            throw new IllegalStateException("Interpretation provider fallback response");
        }

        String content = aiResponse.getContent();
        if (!StringUtils.hasText(content)) {
            logger.warn("[AI_INTERPRETATION] Empty response received provider={} model={} durationMs={}",
                    aiResponse.getProvider(),
                    aiResponse.getModel(),
                    elapsedMillis(startNanos));
            throw new IllegalStateException("Interpretation provider returned empty content");
        }

        String cleaned = stripJsonFences(content.trim());
        try {
            GraduateQueryInterpretation interpretation = objectMapper.readValue(cleaned, GraduateQueryInterpretation.class);
            logger.debug("[AI_INTERPRETATION] Request completed provider={} model={} responseLength={} durationMs={}",
                    aiResponse.getProvider(),
                    aiResponse.getModel(),
                    cleaned.length(),
                    elapsedMillis(startNanos));
            return interpretation;
        } catch (Exception ex) {
            logger.warn("[AI_INTERPRETATION] Failed to parse interpretation JSON provider={} model={} durationMs={} reason={}",
                    aiResponse.getProvider(),
                    aiResponse.getModel(),
                    elapsedMillis(startNanos),
                    ex.getMessage());
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
            throw new IllegalStateException("Failed to parse graduate query interpretation JSON", ex);
        }
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
