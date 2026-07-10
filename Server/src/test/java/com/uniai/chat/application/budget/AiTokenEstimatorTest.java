package com.uniai.chat.application.budget;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.dto.ai.AiRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiTokenEstimatorTest {

    @Test
    void estimateTokensShouldRoundUpAndHandleEmptyText() {
        AiTokenEstimator estimator = new AiTokenEstimator(configuration(4, 12));

        assertEquals(0L, estimator.estimateTokens(""));
        assertEquals(0L, estimator.estimateTokens((String) null));
        assertEquals(1L, estimator.estimateTokens("abcd"));
        assertEquals(2L, estimator.estimateTokens("abcde"));
    }

    @Test
    void estimateRequestTokensShouldIncludeAllComponentsAndOverhead() {
        AiTokenEstimator estimator = new AiTokenEstimator(configuration(4, 12));

        AiRequest request = AiRequest.builder()
                .systemPrompt("abcd")
                .userMessage("abcdef")
                .conversationHistory(List.of(
                        AiConversationMessage.builder().role("user").content("abcd").build(),
                        AiConversationMessage.builder().role("assistant").content("abcd").build()
                ))
                .context(List.of("abcd", "abcdefgh"))
                .build();

        long expected = 1L + 2L + 1L + 1L + 1L + 2L + 12L;
        assertEquals(expected, estimator.estimateRequestTokens(request));
    }

    private AiContextBudgetConfiguration configuration(int charactersPerToken, int overheadTokens) {
        return new AiContextBudgetConfiguration(
                200,
                20,
                50,
                50,
                charactersPerToken,
                overheadTokens,
                java.util.Map.of()
        );
    }
}
