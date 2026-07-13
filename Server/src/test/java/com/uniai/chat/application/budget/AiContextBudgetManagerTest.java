package com.uniai.chat.application.budget;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.dto.ai.AiOperation;
import com.uniai.chat.application.dto.ai.AiRequest;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiContextBudgetManagerTest {

    @Test
    void budgetShouldLeaveFittingRequestUnchanged() {
        AiContextBudgetManager manager = budgetManager("gemini", 200, 20, 50, 50, 4, 0);
        AiRequest request = AiRequest.builder()
                .systemPrompt("abcd")
                .userMessage("efgh")
                .conversationHistory(List.of(
                        AiConversationMessage.builder().role("user").content("abcd").build(),
                        AiConversationMessage.builder().role("assistant").content("abcd").build()
                ))
                .context(List.of("abcd"))
                .build();

        AiContextBudgetResult result = manager.budget(request);

        assertTrue(result.requestFits());
        assertFalse(result.historyTrimmed());
        assertFalse(result.contextTrimmed());
        assertEquals(2, result.finalHistoryCount());
        assertEquals(1, result.finalContextCount());
        assertEquals(20, result.reservedOutputTokens());
        assertEquals(20, result.request().getMaxTokens());
        assertEquals(List.of("abcd", "abcd"), result.request().getConversationHistory().stream().map(AiConversationMessage::getContent).toList());
        assertEquals(List.of("abcd"), result.request().getContext());
    }

    @Test
    void budgetShouldTrimOldestHistoryMessagesFirst() {
        AiContextBudgetManager manager = budgetManager("gemini", 500, 20, 10, 500, 1, 0);
        AiRequest request = AiRequest.builder()
                .systemPrompt("s")
                .userMessage("u")
                .conversationHistory(List.of(
                        msg("user", "aaaa"),
                        msg("assistant", "bbbb"),
                        msg("user", "cccc"),
                        msg("assistant", "dddd"),
                        msg("user", "eeee"),
                        msg("assistant", "ffff")
                ))
                .build();

        AiContextBudgetResult result = manager.budget(request);

        assertTrue(result.historyTrimmed());
        assertTrue(result.requestFits());
        assertEquals(2, result.finalHistoryCount());
        assertEquals(List.of("eeee", "ffff"), result.request().getConversationHistory().stream().map(AiConversationMessage::getContent).toList());
    }

    @Test
    void budgetShouldTrimRetrievalContextAndAppendMarker() {
        AiContextBudgetManager manager = budgetManager("gemini", 500, 20, 500, 20, 1, 0);
        String largeContext = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        AiRequest request = AiRequest.builder()
                .systemPrompt("s")
                .userMessage("u")
                .context(List.of(largeContext))
                .build();

        AiContextBudgetResult result = manager.budget(request);

        assertTrue(result.contextTrimmed());
        assertTrue(result.requestFits());
        assertEquals(2, result.finalContextCount());
        assertEquals("[Retrieved context truncated to fit the configured AI request budget.]",
                result.request().getContext().get(result.request().getContext().size() - 1));
        assertEquals("s", result.request().getSystemPrompt());
        assertEquals("u", result.request().getUserMessage());
    }

    @Test
    void budgetShouldTrimHistoryBeforeRetrievalWhenCombinedBudgetIsTight() {
        AiContextBudgetManager manager = budgetManager("gemini", 45, 5, 100, 100, 1, 0);
        AiRequest request = AiRequest.builder()
                .systemPrompt("ss")
                .userMessage("uu")
                .conversationHistory(List.of(
                        msg("user", "aaaaa"),
                        msg("assistant", "bbbbb"),
                        msg("user", "ccccc"),
                        msg("assistant", "ddddd"),
                        msg("user", "eeeee"),
                        msg("assistant", "fffff")
                ))
                .context(List.of("kkkkkkkkkkkkkkkkkkkk"))
                .build();

        AiContextBudgetResult result = manager.budget(request);

        assertTrue(result.historyTrimmed());
        assertFalse(result.contextTrimmed());
        assertTrue(result.requestFits());
        assertTrue(result.finalHistoryCount() < result.originalHistoryCount());
        assertEquals(1, result.finalContextCount());
        assertEquals("kkkkkkkkkkkkkkkkkkkk", result.request().getContext().get(0));
    }

    @Test
    void budgetShouldRejectImpossibleRequestsBeforeProviderInvocation() {
        AiContextBudgetManager manager = budgetManager("gemini", 10, 5, 10, 10, 4, 0);
        AiRequest request = AiRequest.builder()
                .systemPrompt("system prompt")
                .userMessage("hello")
                .build();

        AiContextBudgetResult result = manager.budget(request);

        assertFalse(result.requestFits());
        assertEquals("AI_CONTEXT_BUDGET_EXCEEDED", result.diagnosticCategory());
        assertEquals("system prompt", result.request().getSystemPrompt());
        assertEquals("hello", result.request().getUserMessage());
    }

    @Test
    void budgetShouldRespectProviderSpecificOverrides() {
        AiContextBudgetConfiguration configuration = configuration(
                100,
                10,
                50,
                50,
                4,
                0,
                Map.of(
                        "gemini", new AiContextBudgetConfiguration.ProviderBudget(200, null, null, null, null),
                        "groq", new AiContextBudgetConfiguration.ProviderBudget(15, null, null, null, null)
                )
        );
        AiTokenEstimator estimator = new AiTokenEstimator(configuration);
        AiRequest request = AiRequest.builder()
                .systemPrompt("abcd")
                .userMessage("abcd")
                .build();

        AiContextBudgetManager geminiManager = new AiContextBudgetManager(configuration, estimator, "gemini");
        AiContextBudgetManager groqManager = new AiContextBudgetManager(configuration, estimator, "groq");
        AiContextBudgetManager unknownManager = new AiContextBudgetManager(configuration, estimator, "unknown");

        assertEquals(200, geminiManager.budget(request).maxInputTokens());
        assertEquals(15, groqManager.budget(request).maxInputTokens());
        assertEquals(100, unknownManager.budget(request).maxInputTokens());
    }

    @Test
    void budgetShouldMarkBlankContextRemovalAsTrimmedWithoutAppendingMarker() {
        AiContextBudgetManager manager = budgetManager("gemini", 500, 20, 500, 500, 1, 0);
        List<String> context = new ArrayList<>(Arrays.asList("official context", "", "   ", null));
        AiRequest request = AiRequest.builder()
                .systemPrompt("s")
                .userMessage("u")
                .context(context)
                .build();

        AiContextBudgetResult result = manager.budget(request);

        assertTrue(result.contextTrimmed());
        assertEquals(List.of("official context"), result.request().getContext());
        assertFalse(result.request().getContext().contains("[Retrieved context truncated to fit the configured AI request budget.]"));
    }

    @Test
    void budgetShouldFitExactlyOnTheConfiguredBoundary() {
        AiContextBudgetManager manager = budgetManager("gemini", 10, 2, 100, 100, 1, 0);
        AiRequest request = AiRequest.builder()
                .systemPrompt("abcd")
                .userMessage("efgh")
                .build();

        AiContextBudgetResult result = manager.budget(request);

        assertTrue(result.requestFits());
        assertEquals(result.maxInputTokens() - result.reservedOutputTokens(), result.finalEstimatedInputTokens());
    }

    @Test
    void budgetShouldRejectWhenRequestIsOneTokenOverTheBoundary() {
        AiContextBudgetManager manager = budgetManager("gemini", 10, 2, 100, 100, 1, 0);
        AiRequest request = AiRequest.builder()
                .systemPrompt("abcde")
                .userMessage("efgh")
                .build();

        AiContextBudgetResult result = manager.budget(request);

        assertFalse(result.requestFits());
        assertEquals("AI_CONTEXT_BUDGET_EXCEEDED", result.diagnosticCategory());
        assertEquals("abcde", result.request().getSystemPrompt());
        assertEquals("efgh", result.request().getUserMessage());
    }

    @Test
    void budgetShouldRecordTokenSummariesAndRejections() {
        AiContextBudgetConfiguration configuration = configuration(
                10,
                2,
                10,
                10,
                1,
                0,
                Map.of("gemini", new AiContextBudgetConfiguration.ProviderBudget(10, 2, 10, 10, 0))
        );
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AiContextBudgetManager manager = new AiContextBudgetManager(configuration, new AiTokenEstimator(configuration), "gemini", meterRegistry);
        AiRequest request = AiRequest.builder()
                .operation(AiOperation.MAIN_RESPONSE)
                .systemPrompt("abcdef")
                .userMessage("ghijkl")
                .context(List.of("mnopqr"))
                .build();

        AiContextBudgetResult result = manager.budget(request);

        assertFalse(result.requestFits());
        assertEquals(1.0, meterRegistry.find("uniai.ai.request.estimated_tokens")
                .tags("operation", "main_response", "provider", "gemini", "stage", "original")
                .summary()
                .count());
        assertEquals(1.0, meterRegistry.find("uniai.ai.request.estimated_tokens")
                .tags("operation", "main_response", "provider", "gemini", "stage", "final")
                .summary()
                .count());
        assertEquals(1.0, meterRegistry.find("uniai.ai.request.estimated_tokens")
                .tags("operation", "main_response", "provider", "gemini", "stage", "reserved_output")
                .summary()
                .count());
        assertEquals(1.0, meterRegistry.find("uniai.ai.budget.rejections")
                .tags("operation", "main_response", "provider", "gemini")
                .counter()
                .count());
    }

    @Test
    void budgetShouldNormalizeProvidersAndFallBackSafelyForBlankOrUnknownValues() {
        AiContextBudgetConfiguration configuration = configuration(
                100,
                10,
                50,
                50,
                4,
                0,
                Map.of(
                        "gemini", new AiContextBudgetConfiguration.ProviderBudget(200, null, null, null, null)
                )
        );
        AiTokenEstimator estimator = new AiTokenEstimator(configuration);
        AiRequest request = AiRequest.builder()
                .systemPrompt("abcd")
                .userMessage("abcd")
                .build();

        assertEquals("placeholder", new AiContextBudgetManager(configuration, estimator, null).budget(request).activeProvider());
        assertEquals("placeholder", new AiContextBudgetManager(configuration, estimator, "   ").budget(request).activeProvider());
        assertEquals("gemini", new AiContextBudgetManager(configuration, estimator, "  GeMiNi  ").budget(request).activeProvider());
        assertEquals("unknown", new AiContextBudgetManager(configuration, estimator, "unknown").budget(request).activeProvider());
        assertEquals(100, new AiContextBudgetManager(configuration, estimator, "unknown").budget(request).maxInputTokens());
    }

    @Test
    void budgetShouldAcceptImmutableHistoryAndContextCollectionsWithoutMutation() {
        AiContextBudgetManager manager = budgetManager("gemini", 500, 20, 500, 500, 4, 0);
        List<AiConversationMessage> history = List.of(
                msg("user", "abcd"),
                msg("assistant", "efgh")
        );
        List<String> context = List.of("ijkl");
        AiRequest request = AiRequest.builder()
                .systemPrompt("mnop")
                .userMessage("qrst")
                .conversationHistory(history)
                .context(context)
                .build();

        AiContextBudgetResult result = manager.budget(request);

        assertTrue(result.requestFits());
        assertEquals(history, request.getConversationHistory());
        assertEquals(context, request.getContext());
        assertNotSame(history, result.request().getConversationHistory());
        assertNotSame(context, result.request().getContext());
    }

    @Test
    void regressionLargeContextShouldBeReducedBelowSmallBudget() {
        AiContextBudgetManager manager = budgetManager("groq", 1200, 100, 100, 200, 4, 0);
        String largeContext = "A".repeat(43161);
        AiRequest request = AiRequest.builder()
                .systemPrompt("s")
                .userMessage("u")
                .context(List.of(largeContext))
                .build();

        AiContextBudgetResult result = manager.budget(request);

        assertTrue(result.contextTrimmed());
        assertTrue(result.requestFits());
        assertTrue(result.finalEstimatedInputTokens() <= 1100);
        assertTrue(result.request().getContext().size() >= 1);
        assertTrue(result.request().getContext().stream().anyMatch(entry -> entry.contains("Retrieved context truncated")));
        assertTrue(result.request().getContext().stream().mapToInt(String::length).sum() < largeContext.length());
    }

    private AiContextBudgetManager budgetManager(
            String provider,
            int maxInputTokens,
            int reservedOutputTokens,
            int maxHistoryTokens,
            int maxRetrievalTokens,
            int charactersPerToken,
            int requestOverheadTokens
    ) {
        AiContextBudgetConfiguration configuration = configuration(
                maxInputTokens,
                reservedOutputTokens,
                maxHistoryTokens,
                maxRetrievalTokens,
                charactersPerToken,
                requestOverheadTokens,
                Map.of(provider, new AiContextBudgetConfiguration.ProviderBudget(
                        maxInputTokens,
                        reservedOutputTokens,
                        maxHistoryTokens,
                        maxRetrievalTokens,
                        requestOverheadTokens
                ))
        );
        return new AiContextBudgetManager(configuration, new AiTokenEstimator(configuration), provider);
    }

    private AiContextBudgetConfiguration configuration(
            int maxInputTokens,
            int reservedOutputTokens,
            int maxHistoryTokens,
            int maxRetrievalTokens,
            int charactersPerToken,
            int requestOverheadTokens,
            Map<String, AiContextBudgetConfiguration.ProviderBudget> providerBudgets
    ) {
        return new AiContextBudgetConfiguration(
                maxInputTokens,
                reservedOutputTokens,
                maxHistoryTokens,
                maxRetrievalTokens,
                charactersPerToken,
                requestOverheadTokens,
                providerBudgets
        );
    }

    private AiConversationMessage msg(String role, String content) {
        return AiConversationMessage.builder()
                .role(role)
                .content(content)
                .build();
    }
}
