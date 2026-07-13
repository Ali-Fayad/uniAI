package com.uniai.chat.infrastructure.ai;

import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.dto.ai.AiOperation;
import com.uniai.chat.application.port.out.AiProviderStatusPort;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.provider.AiProviderFailureCategory;
import com.uniai.chat.infrastructure.metrics.ChatAiMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.util.StringUtils;

/**
 * Placeholder implementation of {@link AiServicePort}.
 * Replace with a real AI provider (e.g. OpenAI, Ollama) without touching any other class.
 */
public class PlaceholderAiServiceAdapter implements AiServicePort {

    private static final Logger logger = LogManager.getLogger(PlaceholderAiServiceAdapter.class);
    private final AiProviderStatusPort statusPort;
    private final MeterRegistry meterRegistry;

    public PlaceholderAiServiceAdapter() {
        this(null, null);
    }

    public PlaceholderAiServiceAdapter(AiProviderStatusPort statusPort) {
        this(statusPort, null);
    }

    public PlaceholderAiServiceAdapter(AiProviderStatusPort statusPort, MeterRegistry meterRegistry) {
        this.statusPort = statusPort;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public AiResponse generateResponse(AiRequest request) {
        String userMessage = request != null ? request.getUserMessage() : null;
        String operation = ChatAiMetrics.normalizeEnumName(request != null ? request.getOperation() : AiOperation.UNKNOWN);
        logger.debug("[PROVIDER] Request received provider=placeholder messageLength={} historyCount={} contextCount={}",
                StringUtils.hasText(userMessage) ? userMessage.length() : 0,
                request != null && request.getConversationHistory() != null ? request.getConversationHistory().size() : 0,
                request != null && request.getContext() != null ? request.getContext().size() : 0);
        ChatAiMetrics.incrementCounter(
                meterRegistry,
                ChatAiMetrics.PROVIDER_REQUESTS,
                "AI provider requests",
                "provider",
                "placeholder",
                "model",
                "placeholder",
                "operation",
                operation,
                "outcome",
                "attempt"
        );
        recordSuccess("placeholder", "placeholder");
        AiResponse response = AiResponse.builder()
                .content("AI response to: " + userMessage)
                .provider("placeholder")
                .model("placeholder")
                .fallback(false)
                .failureCategory(AiProviderFailureCategory.NONE)
                .retryable(false)
                .build();
        logger.debug("[PROVIDER] Request completed provider=placeholder responseLength={}",
                response.getContent() != null ? response.getContent().length() : 0);
        return response;
    }

    private void recordSuccess(String provider, String model) {
        if (statusPort != null) {
            statusPort.recordSuccess(provider, model, 0L);
        }
    }
}
