package com.uniai.chat.infrastructure.config;

import com.uniai.chat.application.budget.AiContextBudgetConfiguration;
import com.uniai.chat.application.budget.AiContextBudgetManager;
import com.uniai.chat.application.budget.AiTokenEstimator;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetConfiguration;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetManager;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationValidator;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQueryInterpreter;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.port.out.GraduateQueryInterpretationPort;
import com.uniai.chat.infrastructure.ai.GeminiAiProperties;
import com.uniai.chat.infrastructure.ai.GeminiAiServiceAdapter;
import com.uniai.chat.infrastructure.ai.GroqAiProperties;
import com.uniai.chat.infrastructure.ai.GroqAiServiceAdapter;
import com.uniai.chat.infrastructure.ai.OllamaAiProperties;
import com.uniai.chat.infrastructure.ai.OllamaAiServiceAdapter;
import com.uniai.chat.infrastructure.ai.PlaceholderAiServiceAdapter;
import com.uniai.chat.infrastructure.interpretation.AiGraduateQueryInterpretationAdapter;
import com.uniai.chat.infrastructure.prompt.GraduateQueryInterpreterPromptProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ChatAiConfiguration {

    private static final Logger logger = LogManager.getLogger(ChatAiConfiguration.class);

    @Bean
    public AiContextBudgetConfiguration aiContextBudgetConfiguration(AiContextBudgetProperties properties) {
        Map<String, AiContextBudgetConfiguration.ProviderBudget> providerBudgets = new LinkedHashMap<>();
        if (properties != null && properties.getProviders() != null) {
            properties.getProviders().forEach((provider, budget) -> providerBudgets.put(provider, new AiContextBudgetConfiguration.ProviderBudget(
                    budget != null ? budget.getMaxInputTokens() : null,
                    budget != null ? budget.getReservedOutputTokens() : null,
                    budget != null ? budget.getMaxHistoryTokens() : null,
                    budget != null ? budget.getMaxRetrievalTokens() : null,
                    budget != null ? budget.getRequestOverheadTokens() : null
            )));
        }

        return new AiContextBudgetConfiguration(
                properties != null ? properties.getMaxInputTokens() : 200000,
                properties != null ? properties.getReservedOutputTokens() : 2000,
                properties != null ? properties.getMaxHistoryTokens() : 12000,
                properties != null ? properties.getMaxRetrievalTokens() : 120000,
                properties != null ? properties.getCharactersPerToken() : 4,
                properties != null ? properties.getRequestOverheadTokens() : 128,
                providerBudgets
        );
    }

    @Bean
    public AiTokenEstimator aiTokenEstimator(AiContextBudgetConfiguration configuration) {
        return new AiTokenEstimator(configuration);
    }

    @Bean
    public AiContextBudgetManager aiContextBudgetManager(
            AiContextBudgetConfiguration configuration,
            AiTokenEstimator estimator,
            @Value("${ai.provider:placeholder}") String provider
    ) {
        return new AiContextBudgetManager(configuration, estimator, provider);
    }

    @Bean
    public GraduateKnowledgeQueryInterpreter graduateKnowledgeQueryInterpreter() {
        return new GraduateKnowledgeQueryInterpreter();
    }

    @Bean
    public GraduateQueryInterpretationBudgetConfiguration graduateQueryInterpretationBudgetConfiguration(
            GraduateQueryInterpretationProperties properties
    ) {
        return new GraduateQueryInterpretationBudgetConfiguration(
                properties != null && properties.isEnabled(),
                properties != null ? properties.getMaxInputTokens() : 1500L,
                properties != null ? properties.getMaxOutputTokens() : 250,
                properties != null ? properties.getHistoryMessageLimit() : 4,
                properties != null ? properties.getPromptPath() : "prompts/graduate-query-interpreter-prompt.txt"
        );
    }

    @Bean
    public GraduateQueryInterpretationBudgetManager graduateQueryInterpretationBudgetManager(
            GraduateQueryInterpretationBudgetConfiguration configuration,
            AiTokenEstimator estimator,
            @Value("${ai.provider:placeholder}") String provider
    ) {
        return new GraduateQueryInterpretationBudgetManager(configuration, estimator, provider);
    }

    @Bean
    public GraduateQueryInterpretationValidator graduateQueryInterpretationValidator() {
        return new GraduateQueryInterpretationValidator();
    }

    @Bean
    public AiServicePort aiServicePort(
            @Value("${ai.provider:placeholder}") String provider,
            GeminiAiProperties geminiAiProperties,
            GroqAiProperties groqAiProperties,
            OllamaAiProperties ollamaAiProperties,
            ObjectMapper objectMapper
    ) {
        String normalizedProvider = normalizeProvider(provider);

        if ("gemini".equals(normalizedProvider)) {
            logger.info("[AI] Provider selected provider=gemini model={}", geminiAiProperties.getModel());
            AiServicePort aiServicePort = new GeminiAiServiceAdapter(geminiAiProperties, objectMapper);
            logger.info("[AI] Provider initialized successfully provider=gemini model={}", geminiAiProperties.getModel());
            return aiServicePort;
        }

        if ("groq".equals(normalizedProvider)) {
            logger.info("[AI] Provider selected provider=groq model={}", groqAiProperties.getModel());
            AiServicePort aiServicePort = new GroqAiServiceAdapter(groqAiProperties);
            logger.info("[AI] Provider initialized successfully provider=groq model={}", groqAiProperties.getModel());
            return aiServicePort;
        }

        if ("ollama".equals(normalizedProvider)) {
            logger.info("[AI] Provider selected provider=ollama model={}", ollamaAiProperties.getModel());
            AiServicePort aiServicePort = new OllamaAiServiceAdapter(ollamaAiProperties);
            logger.info("[AI] Provider initialized successfully provider=ollama model={}", ollamaAiProperties.getModel());
            return aiServicePort;
        }

        if (!"placeholder".equals(normalizedProvider)) {
            logger.warn("[AI] Unsupported provider configured provider={} falling back to placeholder", provider);
        }
        logger.info("[AI] Provider selected provider=placeholder model=placeholder");
        AiServicePort aiServicePort = new PlaceholderAiServiceAdapter();
        logger.info("[AI] Provider initialized successfully provider=placeholder model=placeholder");
        return aiServicePort;
    }

    @Bean
    public GraduateQueryInterpretationPort graduateQueryInterpretationPort(
            @Value("${ai.provider:placeholder}") String provider,
            AiServicePort aiServicePort,
            GraduateQueryInterpreterPromptProvider promptProvider,
            GraduateQueryInterpretationBudgetConfiguration configuration,
            ObjectMapper objectMapper
    ) {
        if (!configuration.enabled() || "placeholder".equals(normalizeProvider(provider))) {
            logger.info("[AI_INTERPRETATION] Interpretation disabled enabled={} provider={}", configuration.enabled(), provider);
            return request -> {
                throw new IllegalStateException("Graduate query interpretation is disabled");
            };
        }

        logger.info("[AI_INTERPRETATION] Interpretation initialized provider={} maxOutputTokens={} historyMessageLimit={}",
                provider,
                configuration.maxOutputTokens(),
                configuration.historyMessageLimit());
        return new AiGraduateQueryInterpretationAdapter(aiServicePort, promptProvider, configuration, objectMapper);
    }

    private String normalizeProvider(String provider) {
        return provider == null ? "" : provider.trim().toLowerCase(Locale.ROOT);
    }
}
