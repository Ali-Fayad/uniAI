package com.uniai.chat.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.catalog.domain.repository.UniversityCatalogRepository;
import com.uniai.chat.application.budget.AiContextBudgetConfiguration;
import com.uniai.chat.application.budget.AiContextBudgetManager;
import com.uniai.chat.application.budget.AiTokenEstimator;
import com.uniai.chat.application.budget.ConversationMemoryBudgetConfiguration;
import com.uniai.chat.application.budget.ConversationMemoryBudgetManager;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetConfiguration;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetManager;
import com.uniai.chat.application.interpretation.CanonicalGraduateQueryDraft;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretation;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationValidator;
import com.uniai.chat.application.memory.ConversationMemoryManager;
import com.uniai.chat.application.memory.ConversationMemoryMergePolicy;
import com.uniai.chat.application.memory.ConversationMemoryTriggerPolicy;
import com.uniai.chat.application.memory.ConversationMemoryUpdatePort;
import com.uniai.chat.application.memory.ConversationMemoryValidator;
import com.uniai.chat.application.port.out.AiProviderStatusPort;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.port.out.ChatTitlePromptPort;
import com.uniai.chat.application.port.out.ConversationMemoryPersistencePort;
import com.uniai.chat.application.port.out.ConversationMemoryPromptPort;
import com.uniai.chat.application.port.out.GraduateQueryInterpretationPort;
import com.uniai.chat.application.port.out.GraduateRoutePlannerPort;
import com.uniai.chat.application.planning.GraduateAiRouteCatalog;
import com.uniai.chat.application.planning.GraduateRoutePlanParser;
import com.uniai.chat.application.planning.GraduateRouteFinalContextBuilder;
import com.uniai.chat.application.planning.GraduateRouteRuntimeManager;
import com.uniai.chat.application.planning.GraduateRoutePlannerShadowManager;
import com.uniai.chat.application.planning.GraduateAiRouteHandler;
import com.uniai.chat.application.planning.GraduateAiRouteRegistry;
import com.uniai.chat.application.planning.GraduateAiRouterManager;
import com.uniai.chat.application.planning.GraduateDirectAiRouteHandler;
import com.uniai.chat.application.planning.GraduateProgramRouteHandlers;
import com.uniai.chat.application.port.out.GraduateProgramRouteDao;
import com.uniai.chat.application.port.out.GraduateTuitionRouteDao;
import com.uniai.chat.application.planning.GraduateTuitionRouteHandlers;
import com.uniai.chat.application.port.out.GraduateCatalogRouteDao;
import com.uniai.chat.application.planning.GraduateCatalogRouteHandlers;
import com.uniai.chat.application.port.out.GraduateSupportRouteDao;
import com.uniai.chat.application.planning.GraduateSupportRouteHandlers;
import com.uniai.chat.application.retrieval.GraduateFollowUpResolver;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQueryInterpreter;
import com.uniai.chat.application.title.ChatTitleGenerationConfiguration;
import com.uniai.chat.application.title.ChatTitleGenerationManager;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.domain.repository.MessageRepository;
import com.uniai.chat.infrastructure.ai.GeminiAiProperties;
import com.uniai.chat.infrastructure.ai.GeminiAiServiceAdapter;
import com.uniai.chat.infrastructure.ai.GroqAiProperties;
import com.uniai.chat.infrastructure.ai.GroqAiServiceAdapter;
import com.uniai.chat.infrastructure.ai.InMemoryAiProviderStatusRegistry;
import com.uniai.chat.infrastructure.ai.OllamaAiProperties;
import com.uniai.chat.infrastructure.ai.OllamaAiServiceAdapter;
import com.uniai.chat.infrastructure.ai.PlaceholderAiServiceAdapter;
import com.uniai.chat.infrastructure.interpretation.AiGraduateQueryInterpretationAdapter;
import com.uniai.chat.infrastructure.interpretation.AiGraduateRoutePlannerAdapter;
import com.uniai.chat.infrastructure.memory.AiConversationMemoryUpdateAdapter;
import com.uniai.chat.infrastructure.prompt.GraduateQueryInterpreterPromptProvider;
import com.uniai.chat.infrastructure.prompt.GraduateRoutePlannerPromptProvider;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ChatAiConfiguration {

    private static final Logger logger = LogManager.getLogger(ChatAiConfiguration.class);

    @Bean
    public AiContextBudgetConfiguration aiContextBudgetConfiguration(
            AiContextBudgetProperties properties) {
        Map<String, AiContextBudgetConfiguration.ProviderBudget> providerBudgets = new LinkedHashMap<>();

        if (properties != null && properties.getProviders() != null) {
            properties.getProviders().forEach((provider, budget) -> providerBudgets.put(
                    provider,
                    new AiContextBudgetConfiguration.ProviderBudget(
                            budget != null ? budget.getMaxInputTokens() : null,
                            budget != null ? budget.getReservedOutputTokens() : null,
                            budget != null ? budget.getMaxHistoryTokens() : null,
                            budget != null ? budget.getMaxRetrievalTokens() : null,
                            budget != null ? budget.getRequestOverheadTokens() : null)));
        }

        return new AiContextBudgetConfiguration(
                properties != null ? properties.getMaxInputTokens() : 200000,
                properties != null ? properties.getReservedOutputTokens() : 2000,
                properties != null ? properties.getMaxHistoryTokens() : 12000,
                properties != null ? properties.getMaxRetrievalTokens() : 120000,
                properties != null ? properties.getCharactersPerToken() : 4,
                properties != null ? properties.getRequestOverheadTokens() : 128,
                providerBudgets);
    }

    @Bean
    public AiTokenEstimator aiTokenEstimator(
            AiContextBudgetConfiguration configuration) {
        return new AiTokenEstimator(configuration);
    }

    @Bean
    public AiContextBudgetManager aiContextBudgetManager(
            AiContextBudgetConfiguration configuration,
            AiTokenEstimator estimator,
            @Value("${ai.provider:placeholder}") String provider,
            MeterRegistry meterRegistry) {
        return new AiContextBudgetManager(
                configuration,
                estimator,
                provider,
                meterRegistry);
    }

    @Bean
    public ConversationMemoryBudgetConfiguration conversationMemoryBudgetConfiguration(
            ConversationMemoryProperties properties) {
        return new ConversationMemoryBudgetConfiguration(
                properties != null && properties.isEnabled(),
                properties != null ? properties.getMaxInputTokens() : 1200L,
                properties != null ? properties.getMaxOutputTokens() : 250,
                properties != null
                        ? properties.getPromptPath()
                        : "prompts/conversation-memory-updater-prompt.txt");
    }

    @Bean
    public ConversationMemoryBudgetManager conversationMemoryBudgetManager(
            ConversationMemoryBudgetConfiguration configuration,
            AiTokenEstimator estimator,
            @Value("${ai.provider:placeholder}") String provider,
            MeterRegistry meterRegistry) {
        return new ConversationMemoryBudgetManager(
                configuration,
                estimator,
                provider,
                meterRegistry);
    }

    @Bean
    public GraduateKnowledgeQueryInterpreter graduateKnowledgeQueryInterpreter() {
        return new GraduateKnowledgeQueryInterpreter();
    }

    @Bean
    public GraduateAiRouteCatalog graduateAiRouteCatalog() {
        return new GraduateAiRouteCatalog();
    }

    @Bean
    public GraduateRoutePlanParser graduateRoutePlanParser(
            GraduateAiRouteCatalog catalog,
            ObjectMapper objectMapper) {
        return new GraduateRoutePlanParser(catalog, objectMapper);
    }

    @Bean
    public GraduateAiRouteRegistry graduateAiRouteRegistry(
            GraduateProgramRouteDao programRouteDao,
            GraduateTuitionRouteDao tuitionRouteDao,
            GraduateCatalogRouteDao catalogRouteDao,
            GraduateSupportRouteDao supportRouteDao,
            ObjectMapper objectMapper) {
        List<GraduateAiRouteHandler<?>> handlers = new ArrayList<>(
                GraduateProgramRouteHandlers.create(programRouteDao));
        handlers.addAll(GraduateTuitionRouteHandlers.create(tuitionRouteDao));
        handlers.addAll(GraduateCatalogRouteHandlers.create(catalogRouteDao));
        handlers.addAll(GraduateSupportRouteHandlers.create(supportRouteDao));
        handlers.add(new GraduateDirectAiRouteHandler(objectMapper));
        return new GraduateAiRouteRegistry(handlers);
    }

    @Bean
    public GraduateAiRouterManager graduateAiRouterManager(
            GraduateRoutePlanParser parser,
            GraduateAiRouteRegistry registry) {
        return new GraduateAiRouterManager(parser, registry);
    }

    @Bean
    public GraduateRouteFinalContextBuilder graduateRouteFinalContextBuilder() {
        return new GraduateRouteFinalContextBuilder();
    }

    @Bean
    public GraduateRouteRuntimeManager graduateRouteRuntimeManager(
            GraduateQueryInterpretationProperties properties,
            GraduateRoutePlannerPort plannerPort,
            GraduateRoutePlannerPromptProvider promptProvider,
            GraduateQueryInterpretationBudgetManager budgetManager,
            GraduateAiRouterManager routerManager,
            GraduateRouteFinalContextBuilder contextBuilder) {
        return new GraduateRouteRuntimeManager(
                properties != null && properties.isRoutePlannerEnabled(),
                plannerPort,
                promptProvider,
                budgetManager,
                routerManager,
                contextBuilder);
    }

    @Bean
    public GraduateRoutePlannerPromptProvider graduateRoutePlannerPromptProvider(
            GraduateQueryInterpretationProperties properties,
            GraduateAiRouteCatalog catalog) {
        return new GraduateRoutePlannerPromptProvider(properties, catalog);
    }

    @Bean
    public GraduateFollowUpResolver graduateFollowUpResolver() {
        return new GraduateFollowUpResolver();
    }

    @Bean
    public GraduateQueryInterpretationBudgetConfiguration graduateQueryInterpretationBudgetConfiguration(
            GraduateQueryInterpretationProperties properties) {
        return new GraduateQueryInterpretationBudgetConfiguration(
                properties != null && properties.isEnabled(),
                properties != null ? properties.getMaxInputTokens() : 1500L,
                properties != null ? properties.getMaxOutputTokens() : 250,
                properties != null ? properties.getHistoryMessageLimit() : 4,
                properties != null
                        ? properties.getPromptPath()
                        : "prompts/graduate-query-interpreter-prompt.txt");
    }

    @Bean
    public GraduateQueryInterpretationBudgetManager graduateQueryInterpretationBudgetManager(
            GraduateQueryInterpretationBudgetConfiguration configuration,
            AiTokenEstimator estimator,
            @Value("${ai.provider:placeholder}") String provider,
            MeterRegistry meterRegistry) {
        return new GraduateQueryInterpretationBudgetManager(
                configuration,
                estimator,
                provider,
                meterRegistry);
    }

    @Bean
    public GraduateQueryInterpretationValidator graduateQueryInterpretationValidator() {
        return new GraduateQueryInterpretationValidator();
    }

    @Bean
    public ConversationMemoryValidator conversationMemoryValidator() {
        return new ConversationMemoryValidator();
    }

    @Bean
    public ConversationMemoryMergePolicy conversationMemoryMergePolicy() {
        return new ConversationMemoryMergePolicy();
    }

    @Bean
    public ConversationMemoryTriggerPolicy conversationMemoryTriggerPolicy() {
        return new ConversationMemoryTriggerPolicy();
    }

    @Bean
    public ConversationMemoryUpdatePort conversationMemoryUpdatePort(
            AiServicePort aiServicePort,
            ConversationMemoryPromptPort promptPort,
            ConversationMemoryBudgetConfiguration budgetConfiguration,
            ObjectMapper objectMapper) {
        return new AiConversationMemoryUpdateAdapter(
                aiServicePort,
                promptPort,
                budgetConfiguration,
                objectMapper);
    }

    @Bean
    public ConversationMemoryManager conversationMemoryManager(
            ConversationMemoryPersistencePort persistencePort,
            ConversationMemoryUpdatePort updatePort,
            ConversationMemoryBudgetManager budgetManager,
            ConversationMemoryValidator validator,
            ConversationMemoryMergePolicy mergePolicy,
            ConversationMemoryTriggerPolicy triggerPolicy,
            UniversityCatalogRepository universityCatalogRepository,
            MessageRepository messageRepository,
            ConversationMemoryPromptPort promptPort,
            ConversationMemoryBudgetConfiguration budgetConfiguration) {
        return new ConversationMemoryManager(
                persistencePort,
                updatePort,
                budgetManager,
                validator,
                mergePolicy,
                triggerPolicy,
                universityCatalogRepository,
                messageRepository,
                promptPort,
                budgetConfiguration);
    }

    @Bean
    public ChatTitleGenerationConfiguration chatTitleGenerationConfiguration(
            @Value("${ai.provider:placeholder}") String provider) {
        String normalizedProvider = normalizeProvider(provider);

        boolean enabled = "gemini".equals(normalizedProvider)
                || "groq".equals(normalizedProvider)
                || "ollama".equals(normalizedProvider);

        return new ChatTitleGenerationConfiguration(
                enabled,
                300L,
                24,
                60);
    }

    @Bean(destroyMethod = "shutdown")
    public Executor chatTitleExecutor() {
        return Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(
                    runnable,
                    "chat-title-generator");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Bean(destroyMethod = "shutdown")
    public Executor graduateRoutePlannerShadowExecutor() {
        return Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "graduate-route-planner-shadow");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Bean
    public GraduateRoutePlannerShadowManager graduateRoutePlannerShadowManager(
            GraduateQueryInterpretationProperties properties,
            GraduateRoutePlannerPort plannerPort,
            GraduateRoutePlannerPromptProvider promptProvider,
            GraduateQueryInterpretationBudgetManager budgetManager,
            @Qualifier("graduateRoutePlannerShadowExecutor") Executor executor) {
        return new GraduateRoutePlannerShadowManager(
                properties != null && properties.isRoutePlannerShadowEnabled(),
                plannerPort,
                promptProvider,
                budgetManager,
                executor);
    }

    @Bean
    public ChatTitleGenerationManager chatTitleGenerationManager(
            ChatRepository chatRepository,
            AiServicePort aiServicePort,
            ChatTitlePromptPort promptPort,
            AiTokenEstimator estimator,
            ChatTitleGenerationConfiguration configuration,
            @Value("${ai.provider:placeholder}") String provider,
            @Qualifier("chatTitleExecutor") Executor chatTitleExecutor,
            MeterRegistry meterRegistry) {
        return new ChatTitleGenerationManager(
                chatRepository,
                aiServicePort,
                promptPort,
                estimator,
                configuration,
                chatTitleExecutor,
                provider,
                meterRegistry);
    }

    @Bean
    public AiProviderStatusPort aiProviderStatusPort() {
        return new InMemoryAiProviderStatusRegistry();
    }

    @Bean
    public AiServicePort aiServicePort(
            @Value("${ai.provider:placeholder}") String provider,
            GeminiAiProperties geminiAiProperties,
            GroqAiProperties groqAiProperties,
            OllamaAiProperties ollamaAiProperties,
            AiProviderStatusPort aiProviderStatusPort,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry) {
        String normalizedProvider = normalizeProvider(provider);

        if ("gemini".equals(normalizedProvider)) {
            logger.info(
                    "[AI] Provider selected provider=gemini model={}",
                    geminiAiProperties.getModel());

            AiServicePort aiServicePort = new GeminiAiServiceAdapter(
                    geminiAiProperties,
                    objectMapper,
                    aiProviderStatusPort,
                    meterRegistry);

            logger.info(
                    "[AI] Provider initialized successfully provider=gemini model={}",
                    geminiAiProperties.getModel());

            return aiServicePort;
        }

        if ("groq".equals(normalizedProvider)) {
            logger.info(
                    "[AI] Provider selected provider=groq model={}",
                    groqAiProperties.getModel());

            AiServicePort aiServicePort = new GroqAiServiceAdapter(
                    groqAiProperties,
                    objectMapper,
                    aiProviderStatusPort,
                    meterRegistry);

            logger.info(
                    "[AI] Provider initialized successfully provider=groq model={}",
                    groqAiProperties.getModel());

            return aiServicePort;
        }

        if ("ollama".equals(normalizedProvider)) {
            logger.info(
                    "[AI] Provider selected provider=ollama model={}",
                    ollamaAiProperties.getModel());

            AiServicePort aiServicePort = new OllamaAiServiceAdapter(
                    ollamaAiProperties,
                    objectMapper,
                    aiProviderStatusPort,
                    meterRegistry);

            logger.info(
                    "[AI] Provider initialized successfully provider=ollama model={}",
                    ollamaAiProperties.getModel());

            return aiServicePort;
        }

        if (!"placeholder".equals(normalizedProvider)) {
            logger.warn(
                    "[AI] Unsupported provider configured provider={} falling back to placeholder",
                    provider);
        }

        logger.info(
                "[AI] Provider selected provider=placeholder model=placeholder");

        AiServicePort aiServicePort = new PlaceholderAiServiceAdapter(
                aiProviderStatusPort,
                meterRegistry);

        logger.info(
                "[AI] Provider initialized successfully provider=placeholder model=placeholder");

        return aiServicePort;
    }

    public AiServicePort aiServicePort(
            String provider,
            GeminiAiProperties geminiAiProperties,
            GroqAiProperties groqAiProperties,
            OllamaAiProperties ollamaAiProperties,
            AiProviderStatusPort aiProviderStatusPort,
            ObjectMapper objectMapper) {
        return aiServicePort(
                provider,
                geminiAiProperties,
                groqAiProperties,
                ollamaAiProperties,
                aiProviderStatusPort,
                objectMapper,
                null);
    }

    @Bean
    public GraduateQueryInterpretationPort graduateQueryInterpretationPort(
            @Value("${ai.provider:placeholder}") String provider,
            AiServicePort aiServicePort,
            GraduateQueryInterpreterPromptProvider promptProvider,
            GraduateQueryInterpretationBudgetConfiguration configuration,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry) {
        if (!configuration.enabled()
                || "placeholder".equals(normalizeProvider(provider))) {

            logger.info(
                    "[AI_INTERPRETATION] Interpretation disabled enabled={} provider={}",
                    configuration.enabled(),
                    provider);

            return new GraduateQueryInterpretationPort() {

                @Override
                public GraduateQueryInterpretation interpret(
                        GraduateQueryInterpretationRequest request) {
                    throw new IllegalStateException(
                            "Graduate query interpretation is disabled");
                }

                @Override
                public CanonicalGraduateQueryDraft interpretDraft(
                        GraduateQueryInterpretationRequest request) {
                    throw new IllegalStateException(
                            "Graduate query interpretation is disabled");
                }
            };
        }

        logger.info(
                "[AI_INTERPRETATION] Interpretation initialized provider={} maxOutputTokens={} historyMessageLimit={}",
                provider,
                configuration.maxOutputTokens(),
                configuration.historyMessageLimit());

        return new AiGraduateQueryInterpretationAdapter(
                aiServicePort,
                promptProvider,
                configuration,
                objectMapper,
                meterRegistry);
    }

    @Bean
    public GraduateRoutePlannerPort graduateRoutePlannerPort(
            @Value("${ai.provider:placeholder}") String provider,
            AiServicePort aiServicePort,
            GraduateRoutePlannerPromptProvider promptProvider,
            GraduateQueryInterpretationBudgetConfiguration configuration,
            GraduateRoutePlanParser parser) {
        if (!configuration.enabled() || "placeholder".equals(normalizeProvider(provider))) {
            return request -> {
                throw new IllegalStateException("Graduate route planning is disabled");
            };
        }
        return new AiGraduateRoutePlannerAdapter(aiServicePort, promptProvider, configuration, parser);
    }

    private String normalizeProvider(String provider) {
        return provider == null
                ? ""
                : provider.trim().toLowerCase(Locale.ROOT);
    }
}
