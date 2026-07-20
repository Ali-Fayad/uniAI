package com.uniai.chat.application.service;

import com.uniai.chat.application.dto.command.SendMessageCommand;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.dto.ai.AiOperation;
import com.uniai.chat.application.budget.AiContextBudgetManager;
import com.uniai.chat.application.budget.AiContextBudgetResult;
import com.uniai.chat.application.citation.GraduateCitation;
import com.uniai.chat.application.citation.GraduateCitationDto;
import com.uniai.chat.application.citation.GraduateCitationEngine;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.provider.AiProviderFailureCategory;
import com.uniai.chat.application.dto.response.ChatCreationResponseDto;
import com.uniai.chat.application.dto.response.ChatSummaryResponseDto;
import com.uniai.chat.application.dto.response.MessageResponseDto;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.ConversationMemoryManager;
import com.uniai.chat.application.title.ChatTitleGenerationManager;
import com.uniai.chat.application.port.in.*;
import com.uniai.chat.application.port.out.AiProviderStatusPort;
import com.uniai.chat.application.port.out.ChatSystemPromptPort;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.planning.GraduateRouteRuntimeManager;
import com.uniai.chat.application.planning.GraduateRouteRuntimeOutcome;
import com.uniai.chat.application.planning.GraduateRouteExecutionResult;
import com.uniai.chat.domain.builder.ChatBuilder;
import com.uniai.chat.domain.builder.MessageBuilder;
import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.model.Message;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.domain.repository.MessageRepository;
import com.uniai.catalog.domain.model.UniversityCatalog;
import com.uniai.catalog.domain.repository.UniversityCatalogRepository;
import com.uniai.shared.exception.ChatNotFoundException;
import com.uniai.shared.exception.EmailNotFoundException;
import com.uniai.shared.exception.InvalidMessageException;
import com.uniai.shared.exception.UnauthorizedAccessException;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.uniai.chat.application.provider.AiProviderStatusSnapshot;
import com.uniai.chat.infrastructure.metrics.ChatAiMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Application service for all chat use cases.
 */
@Service
public class ChatApplicationService implements
        CreateChatUseCase,
        SendMessageUseCase,
        GetUserChatsUseCase,
        GetChatMessagesUseCase,
        DeleteChatUseCase,
        DeleteAllChatsUseCase {

    private static final Logger logger = LogManager.getLogger(ChatApplicationService.class);
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AiServicePort aiServicePort;
    private final ChatSystemPromptPort chatSystemPromptPort;
    private final UniversityCatalogRepository universityCatalogRepository;
    private final AiContextBudgetManager aiContextBudgetManager;
    private final ConversationMemoryManager conversationMemoryManager;
    private final ChatTitleGenerationManager chatTitleGenerationManager;
    private final AiProviderStatusPort aiProviderStatusPort;
    private final MeterRegistry meterRegistry;
    private GraduateRouteRuntimeManager routePlannerRuntimeManager;

    private static final int MAX_CONVERSATION_HISTORY_MESSAGES = 6;
    private static final int MAX_INTERPRETATION_HISTORY_MESSAGES = 4;

    @Autowired
    public ChatApplicationService(
            ChatRepository chatRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            AiServicePort aiServicePort,
            ChatSystemPromptPort chatSystemPromptPort,
            UniversityCatalogRepository universityCatalogRepository,
            GraduateRouteRuntimeManager routePlannerRuntimeManager,
            AiContextBudgetManager aiContextBudgetManager,
            ConversationMemoryManager conversationMemoryManager,
            ChatTitleGenerationManager chatTitleGenerationManager,
            AiProviderStatusPort aiProviderStatusPort,
            MeterRegistry meterRegistry
    ) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.aiServicePort = aiServicePort;
        this.chatSystemPromptPort = chatSystemPromptPort;
        this.universityCatalogRepository = universityCatalogRepository;
        this.aiContextBudgetManager = aiContextBudgetManager;
        this.conversationMemoryManager = conversationMemoryManager;
        this.chatTitleGenerationManager = chatTitleGenerationManager;
        this.aiProviderStatusPort = aiProviderStatusPort;
        this.meterRegistry = meterRegistry;
        this.routePlannerRuntimeManager = routePlannerRuntimeManager;
    }

    // -------------------------------------------------------------------------
    // CreateChatUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ChatCreationResponseDto createChat(String email) {
        User user = getUser(email);
        Chat chat = ChatBuilder.forUser(user).build();
        chatRepository.save(chat);
        return ChatCreationResponseDto.builder().chatId(chat.getId()).build();
    }

    // -------------------------------------------------------------------------
    // SendMessageUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public MessageResponseDto sendMessage(String email, SendMessageCommand command) {
        long requestStartNanos = System.nanoTime();
        Long chatId = command != null ? command.getChatId() : null;
        Long userId = null;
        String chatOutcome = "failure";
        logger.info("[CHAT] Request received chatId={} messageLength={}",
                chatId,
                command != null && command.getContent() != null ? command.getContent().length() : 0);

        try {
            validateContent(command.getContent());

            Chat chat = chatRepository.findById(command.getChatId())
                    .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

            User user = getUser(email);
            userId = user.getId();
            validateOwnership(chat, user);
            ConversationMemory conversationMemory = conversationMemoryManager == null
                    ? ConversationMemory.empty()
                    : conversationMemoryManager.loadMemory(chat.getId());

            logger.info("[CHAT] Request started userId={} chatId={} messageLength={}",
                    user.getId(),
                    chat.getId(),
                    command.getContent() != null ? command.getContent().length() : 0);

            Message userMessage = MessageBuilder.userMessage(chat, user.getId(), command.getContent()).build();
            long userSaveStartNanos = System.nanoTime();
            Message persistedUserMessage = messageRepository.save(userMessage);
            logger.debug("[PERSISTENCE] User message saved id={} chatId={} durationMs={}",
                    persistedUserMessage != null ? persistedUserMessage.getId() : null,
                    chat.getId(),
                    elapsedMillis(userSaveStartNanos));

            boolean isFirstUserTurn = messageRepository.countByChatId(chat.getId()) == 1L;

            logger.debug("[CHAT] History retrieval started chatId={}", chat.getId());
            long historyStartNanos = System.nanoTime();
            List<AiConversationMessage> conversationHistory = loadRecentConversationHistory(
                    chat.getId(),
                    user.getId(),
                    command.getContent()
            );
            logger.debug("[CHAT] History retrieval completed chatId={} messageCount={} durationMs={}",
                    chat.getId(),
                    conversationHistory.size(),
                    elapsedMillis(historyStartNanos));

            List<AiConversationMessage> recentConversationWindow = buildRecentConversationWindow(conversationHistory);
            logger.debug("[CHAT] Recent conversation window prepared chatId={} windowMessageCount={}",
                    chat.getId(),
                    recentConversationWindow.size());
            List<UniversityCatalog> universityCatalogs = universityCatalogRepository.findAll();
            GraduateRouteRuntimeOutcome routeOutcome = routePlannerRuntimeManager.execute(
                    command.getContent(), recentConversationWindow, conversationMemory, universityCatalogs);
            GraduateRouteExecutionResult activeRouteExecution = routeOutcome.executionResult();
            boolean generalChat = activeRouteExecution.route()
                    == com.uniai.chat.application.planning.GraduateAiRoute.DIRECT_AI_RESPONSE;
            String graduateContext = generalChat ? null : routeOutcome.finalContext();
            List<GraduateCitation> graduateCitations = activeRouteExecution.citations();
            logger.info("[RETRIEVAL] Route runtime selected chatId={} route={} empty={} citationCount={}",
                    chat.getId(), activeRouteExecution.route(), activeRouteExecution.empty(),
                    graduateCitations.size());
            List<String> context = (graduateContext != null && !graduateContext.isBlank())
                    ? List.of(graduateContext)
                    : Collections.emptyList();
            if (!generalChat && context.isEmpty()) {
                logger.warn("[RETRIEVAL] Empty context returned chatId={}", chat.getId());
            }

            String baseSystemPrompt = chatSystemPromptPort.getPrompt();
            if (activeRouteExecution != null && generalChat) {
                baseSystemPrompt += "\n\nThe route planner selected DIRECT_AI_RESPONSE with reason "
                        + activeRouteExecution.directAiReason()
                        + ". No Graduate Knowledge database retrieval was performed.";
            }
            String systemPrompt = generalChat
                    ? baseSystemPrompt
                    : GraduateCitationEngine.appendCitationInstructions(baseSystemPrompt, graduateCitations);
            logger.debug("[PROMPT] System prompt loaded promptLength={}",
                    StringUtils.hasText(systemPrompt) ? systemPrompt.length() : 0);

            logger.debug("[AI] Request creation started chatId={} providerBean={}",
                    chat.getId(),
                    aiServicePort.getClass().getSimpleName());
            AiRequest aiRequest = AiRequest.builder()
                    .userMessage(command.getContent())
                    .systemPrompt(systemPrompt)
                    .conversationHistory(conversationHistory)
                    .context(context)
                    .conversationMemory(conversationMemory)
                    .operation(AiOperation.MAIN_RESPONSE)
                    .build();

            logger.debug("[AI] Budget evaluation started chatId={} providerBean={}",
                    chat.getId(),
                    aiServicePort.getClass().getSimpleName());
            AiContextBudgetResult budgetResult = aiContextBudgetManager.budget(aiRequest);
            logger.debug("[AI] Budget evaluation completed chatId={} provider={} requestFits={} originalEstimatedInputTokens={} finalEstimatedInputTokens={} historyTrimmed={} contextTrimmed={} finalHistoryCount={} finalContextCount={}",
                    chat.getId(),
                    budgetResult.activeProvider(),
                    budgetResult.requestFits(),
                    budgetResult.originalEstimatedInputTokens(),
                    budgetResult.finalEstimatedInputTokens(),
                    budgetResult.historyTrimmed(),
                    budgetResult.contextTrimmed(),
                    budgetResult.finalHistoryCount(),
                    budgetResult.finalContextCount());

            AiResponse aiResponse;
            boolean budgetRejected = false;
            long providerDurationMs = 0L;
            AiRequest budgetedRequest = budgetResult.request();
            List<GraduateCitation> activeGraduateCitations = generalChat
                    ? List.of()
                    : budgetResult.contextTrimmed()
                    ? GraduateCitationEngine.filterCitationsPresentInContext(
                    graduateCitations,
                    budgetedRequest != null ? budgetedRequest.getContext() : List.of()
            )
                    : graduateCitations;
            String activeSystemPrompt = generalChat
                    ? baseSystemPrompt
                    : GraduateCitationEngine.appendCitationInstructions(baseSystemPrompt, activeGraduateCitations);
            if (!budgetResult.requestFits()) {
                budgetRejected = true;
                chatOutcome = "budget_rejected";
                ChatAiMetrics.incrementCounter(
                        meterRegistry,
                        ChatAiMetrics.FALLBACKS,
                        "AI fallback usage",
                        "operation",
                        "main_response",
                        "reason",
                        "budget_rejected"
                );
                logger.warn("[AI] Budget rejection chatId={} provider={} category={} originalEstimatedInputTokens={} finalEstimatedInputTokens={} maxInputTokens={} reservedOutputTokens={} historyTrimmed={} contextTrimmed={} requestFits={}",
                        chat.getId(),
                        budgetResult.activeProvider(),
                        budgetResult.diagnosticCategory(),
                        budgetResult.originalEstimatedInputTokens(),
                        budgetResult.finalEstimatedInputTokens(),
                        budgetResult.maxInputTokens(),
                        budgetResult.reservedOutputTokens(),
                        budgetResult.historyTrimmed(),
                        budgetResult.contextTrimmed(),
                        budgetResult.requestFits());
                aiResponse = AiResponse.builder()
                        .content("AI service error : this message is from ChatApplicationService. Please try again later.")
                        .provider(budgetResult.activeProvider())
                        .fallback(true)
                        .failureCategory(AiProviderFailureCategory.UNKNOWN)
                        .retryable(false)
                        .build();
            } else {
                AiRequest effectiveRequest = budgetedRequest == null
                        ? null
                        : AiRequest.builder()
                        .userMessage(budgetedRequest.getUserMessage())
                        .systemPrompt(activeSystemPrompt)
                        .conversationHistory(budgetedRequest.getConversationHistory())
                        .context(budgetedRequest.getContext())
                        .conversationMemory(budgetedRequest.getConversationMemory())
                        .operation(AiOperation.MAIN_RESPONSE)
                        .temperature(budgetedRequest.getTemperature())
                        .maxTokens(budgetedRequest.getMaxTokens())
                        .build();
                logger.debug("[AI] Provider invocation started chatId={} providerBean={} historyCount={} contextCount={} maxTokens={}",
                        chat.getId(),
                        aiServicePort.getClass().getSimpleName(),
                        effectiveRequest != null && effectiveRequest.getConversationHistory() != null ? effectiveRequest.getConversationHistory().size() : 0,
                        effectiveRequest != null && effectiveRequest.getContext() != null ? effectiveRequest.getContext().size() : 0,
                        effectiveRequest != null ? effectiveRequest.getMaxTokens() : null);
                long providerStartNanos = System.nanoTime();
                aiResponse = aiServicePort.generateResponse(effectiveRequest);
                providerDurationMs = elapsedMillis(providerStartNanos);
                recordMainResponseDuration(budgetResult.activeProvider(), aiResponse, providerDurationMs);
            }

            String aiContent = (aiResponse != null && aiResponse.getContent() != null)
                    ? aiResponse.getContent()
                    : "AI service error : this message is from ChatApplicationService. Please try again later.";
            if (budgetRejected) {
                logger.debug("[AI] Budget fallback response prepared chatId={} provider={} responseLength={}",
                        chat.getId(),
                        aiResponse != null ? aiResponse.getProvider() : budgetResult.activeProvider(),
                        aiContent.length());
            } else if (aiResponse == null) {
                chatOutcome = "failure";
                logger.warn("[AI] Provider returned null response chatId={} durationMs={}", chat.getId(), providerDurationMs);
            } else {
                if (!StringUtils.hasText(aiResponse.getContent())) {
                    logger.warn("[AI] Provider returned empty response provider={} model={} chatId={} durationMs={}",
                            aiResponse.getProvider(),
                            aiResponse.getModel(),
                            chat.getId(),
                            providerDurationMs);
                }
                if (Boolean.TRUE.equals(aiResponse.getFallback())) {
                    chatOutcome = "provider_fallback";
                    logger.warn("[AI] Provider fallback used provider={} model={} failureCategory={} retryable={} chatId={} durationMs={}",
                            aiResponse.getProvider(),
                            aiResponse.getModel(),
                            aiResponse.getFailureCategory(),
                            aiResponse.getRetryable(),
                            chat.getId(),
                            providerDurationMs);
                } else {
                    chatOutcome = generalChat ? "general_chat" : "success";
                    logger.debug("[AI] Provider success provider={} model={} finishReason={} responseLength={} chatId={} durationMs={}",
                            aiResponse.getProvider(),
                            aiResponse.getModel(),
                            aiResponse.getFinishReason(),
                            aiContent.length(),
                            chat.getId(),
                            providerDurationMs);
                }
            }
            List<GraduateCitationDto> responseCitations = toCitationDtos(
                    GraduateCitationEngine.extractCitations(aiContent, activeGraduateCitations)
            );
            Message aiMessage = MessageBuilder.aiMessage(chat, aiContent).build();
            long aiSaveStartNanos = System.nanoTime();
            Message persistedAiMessage = messageRepository.save(aiMessage);
            logger.debug("[PERSISTENCE] Assistant message saved id={} chatId={} durationMs={}",
                    persistedAiMessage != null ? persistedAiMessage.getId() : null,
                    chat.getId(),
                    elapsedMillis(aiSaveStartNanos));

            chat.setUpdatedAt(LocalDateTime.now());
            chatRepository.save(chat);
            registerChatTitleGeneration(chat.getId(), command.getContent(), isFirstUserTurn);
            registerConversationMemoryUpdate(chat.getId(), conversationMemory, command.getContent(), aiContent, activeRouteExecution);

            logger.info("[CHAT] Request completed userId={} chatId={} assistantMessageId={} responseLength={} durationMs={}",
                    user.getId(),
                    chat.getId(),
                    persistedAiMessage != null ? persistedAiMessage.getId() : null,
                    aiContent.length(),
                    elapsedMillis(requestStartNanos));

            return toDto(aiMessage, responseCitations);
        } catch (RuntimeException ex) {
            chatOutcome = "failure";
            logger.error("[CHAT] Request failed userId={} chatId={} durationMs={} reason={}",
                    userId,
                    chatId,
                    elapsedMillis(requestStartNanos),
                    ex.getMessage(),
                    ex);
            throw ex;
        } finally {
            recordChatRequestDuration(requestStartNanos, chatOutcome);
        }
    }

    // -------------------------------------------------------------------------
    // GetUserChatsUseCase
    // -------------------------------------------------------------------------

    @Override
    public List<ChatSummaryResponseDto> getUserChats(String email) {
        User user = getUser(email);
        return chatRepository.findByUserUsernameOrderByUpdatedAtDesc(user.getUsername())
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    // -------------------------------------------------------------------------
    // GetChatMessagesUseCase
    // -------------------------------------------------------------------------

    @Override
    public List<MessageResponseDto> getChatMessages(String email, Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));
        User user = getUser(email);
        validateOwnership(chat, user);

        return messageRepository.findByChatIdOrderByTimestampAsc(chatId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // -------------------------------------------------------------------------
    // DeleteChatUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void deleteChat(String email, Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));
        User user = getUser(email);
        validateOwnership(chat, user);

        messageRepository.deleteByChatId(chatId);
        chatRepository.delete(chat);
    }

    // -------------------------------------------------------------------------
    // DeleteAllChatsUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void deleteAllChats(String email) {
        User user = getUser(email);
        List<Chat> chats = chatRepository.findByUserUsernameOrderByUpdatedAtDesc(user.getUsername());
        if (chats.isEmpty()) return;

        List<Long> chatIds = chats.stream().map(Chat::getId).toList();
        messageRepository.deleteByChatIdIn(chatIds);
        chatRepository.deleteAll(chats);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(EmailNotFoundException::new);
    }

    private void validateOwnership(Chat chat, User user) {
        if (!chat.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to access this chat");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidMessageException("Message content cannot be empty");
        }
        if (content.length() > 5000) {
            throw new InvalidMessageException("Message content is too long (max 5000 characters)");
        }
    }

    private List<AiConversationMessage> loadRecentConversationHistory(Long chatId, Long currentUserId, String currentUserContent) {
        List<Message> messages = messageRepository.findByChatIdOrderByTimestampAsc(chatId);
        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        int endIndex = messages.size();
        Message lastMessage = messages.get(endIndex - 1);
        if (lastMessage != null
                && lastMessage.getSenderId() != null
                && lastMessage.getSenderId().equals(currentUserId)
                && lastMessage.getContent() != null
                && lastMessage.getContent().equals(currentUserContent)) {
            endIndex--;
        }

        if (endIndex <= 0) {
            return Collections.emptyList();
        }

        int startIndex = Math.max(0, endIndex - MAX_CONVERSATION_HISTORY_MESSAGES);
        List<AiConversationMessage> history = new ArrayList<>();
        for (Message message : messages.subList(startIndex, endIndex)) {
            if (message != null && message.getSenderId() != null && message.getSenderId().equals(0L)
                    && isAssistantErrorMessage(message.getContent())) {
                continue;
            }
            history.add(AiConversationMessage.builder()
                    .role(resolveConversationRole(message))
                    .content(message.getContent())
                    .build());
        }
        return history;
    }

    private boolean isAssistantErrorMessage(String content) {
        if (!StringUtils.hasText(content)) return false;
        String normalized = content.trim().toLowerCase();
        return normalized.contains("please try again later")
                || normalized.contains("temporarily unavailable")
                || normalized.contains("provider error")
                || normalized.contains("service error")
                || normalized.contains("request failed");
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private List<AiConversationMessage> buildRecentConversationWindow(List<AiConversationMessage> conversationHistory) {
        if (conversationHistory == null || conversationHistory.isEmpty()) return Collections.emptyList();
        int startIndex = Math.max(0, conversationHistory.size() - MAX_INTERPRETATION_HISTORY_MESSAGES);
        return List.copyOf(conversationHistory.subList(startIndex, conversationHistory.size()));
    }

    private String buildClarificationMessage(String reason) {
        if (reason == null) {
            return buildAmbiguousGraduateMessage();
        }
        String normalized = reason.trim().toUpperCase();
        if (normalized.contains("DEGREE")) {
            return "Which degree type should I use?";
        }
        if (normalized.contains("COMPARISON")) {
            return "Do you mean the first or second university?";
        }
        if (normalized.contains("INTENT")) {
            return "Do you mean programs or tuition?";
        }
        if (normalized.contains("UNIVERSITY")) {
            return "Which university are you referring to?";
        }
        return buildAmbiguousGraduateMessage();
    }

    private boolean isUnsupportedGraduateDegreeRequest(String message) {
        if (!StringUtils.hasText(message)) {
            return false;
        }
        String normalized = message.trim().toLowerCase();
        return normalized.contains("bachelor")
                || normalized.contains("bachlour")
                || normalized.contains("bachlor")
                || normalized.contains("undergraduate")
                || normalized.contains("undergrad");
    }

    private String buildUnsupportedGraduateMessage() {
        return "I can help with master's and PhD graduate questions only.";
    }

    private String buildAmbiguousGraduateMessage() {
        return "I need a clearer university reference to answer safely.";
    }

    private String buildFallbackUsedMessage(String failureCategory) {
        return "Graduate query interpretation fell back to deterministic logic.";
    }

    private void recordChatRequestDuration(long startNanos, String outcome) {
        ChatAiMetrics.recordTimer(
                meterRegistry,
                ChatAiMetrics.CHAT_REQUEST_DURATION,
                "Total duration of ChatApplicationService.sendMessage",
                System.nanoTime() - startNanos,
                "outcome",
                ChatAiMetrics.normalizeTagValue(outcome)
        );
    }

    private void recordMainResponseDuration(String provider, AiResponse response, long durationMs) {
        if (durationMs < 0) {
            return;
        }
        String resolvedProvider = StringUtils.hasText(provider) ? provider : "unknown";
        AiProviderStatusSnapshot snapshot = resolveProviderSnapshot(resolvedProvider);
        String model = response != null && StringUtils.hasText(response.getModel())
                ? response.getModel()
                : snapshot != null && StringUtils.hasText(snapshot.model()) ? snapshot.model() : "unknown";
        String outcome;
        if (response == null) {
            outcome = "failure";
        } else if (Boolean.TRUE.equals(response.getFallback())) {
            outcome = "provider_fallback";
        } else if (!StringUtils.hasText(response.getContent())) {
            outcome = "failure";
        } else {
            outcome = "success";
        }
        ChatAiMetrics.recordTimer(
                meterRegistry,
                ChatAiMetrics.RESPONSE_DURATION,
                "Duration of the main answer-generation provider call",
                Duration.ofMillis(durationMs).toNanos(),
                "provider",
                resolvedProvider,
                "model",
                model,
                "outcome",
                outcome,
                "failure_category",
                response != null && response.getFailureCategory() != null
                        ? ChatAiMetrics.normalizeEnumName(response.getFailureCategory())
                        : "unknown"
        );
    }

    private boolean isResolvedNoData(String formattedContext) {
        if (!StringUtils.hasText(formattedContext)) {
            return true;
        }
        String normalized = formattedContext.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("no matching rows")
                || normalized.contains("no matching campus")
                || normalized.contains("no matching program")
                || normalized.contains("location data is unavailable");
    }

    private String resolveCurrentProviderName() {
        if (aiServicePort == null) {
            return "unknown";
        }
        String simpleName = aiServicePort.getClass().getSimpleName();
        String lower = simpleName == null ? "" : simpleName.toLowerCase();
        if (lower.contains("gemini")) {
            return "gemini";
        }
        if (lower.contains("groq")) {
            return "groq";
        }
        if (lower.contains("ollama")) {
            return "ollama";
        }
        if (lower.contains("placeholder")) {
            return "placeholder";
        }
        return "unknown";
    }

    private AiProviderStatusSnapshot resolveProviderSnapshot(String provider) {
        if (aiProviderStatusPort == null || !StringUtils.hasText(provider)) {
            return AiProviderStatusSnapshot.unknown(StringUtils.hasText(provider) ? provider : "unknown");
        }
        AiProviderStatusSnapshot snapshot = aiProviderStatusPort.getStatus(provider);
        return snapshot == null ? AiProviderStatusSnapshot.unknown(provider) : snapshot;
    }

    private void registerConversationMemoryUpdate(
            Long chatId,
            ConversationMemory previousMemory,
            String currentUserMessage,
            String assistantResponse,
            GraduateRouteExecutionResult routeResult
    ) {
        if (conversationMemoryManager == null || chatId == null) {
            return;
        }

        Runnable task = () -> {
            try {
                conversationMemoryManager.updateMemoryIfNeeded(chatId, previousMemory, currentUserMessage, assistantResponse, routeResult);
            } catch (RuntimeException ex) {
                logger.warn("[AI_MEMORY] Memory update failed chatId={} reason={}", chatId, ex.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    private String resolveConversationRole(Message message) {
        if (message == null || message.getSenderId() == null) {
            return "user";
        }
        return message.getSenderId() == 0L ? "assistant" : "user";
    }

    private MessageResponseDto toDto(Message message) {
        return toDto(message, List.of());
    }

    private MessageResponseDto toDto(Message message, List<GraduateCitationDto> citations) {
        return MessageResponseDto.builder()
                .messageId(message.getId())
                .chatId(message.getChatId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .citations(citations == null ? List.of() : List.copyOf(citations))
                .build();
    }

    private List<GraduateCitationDto> toCitationDtos(List<GraduateCitation> citations) {
        if (citations == null || citations.isEmpty()) {
            return List.of();
        }
        return citations.stream()
                .map(GraduateCitationDto::from)
                .filter(Objects::nonNull)
                .toList();
    }

    private ChatSummaryResponseDto toSummaryDto(Chat chat) {
        return ChatSummaryResponseDto.builder()
                .id(chat.getId())
                .title(chat.getTitle())
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .build();
    }

    private void registerChatTitleGeneration(Long chatId, String firstUserMessage, boolean shouldGenerate) {
        if (chatTitleGenerationManager == null || chatId == null || !shouldGenerate) {
            return;
        }

        Runnable task = () -> {
            try {
                chatTitleGenerationManager.generateTitleIfNeeded(chatId, firstUserMessage);
            } catch (RuntimeException ex) {
                logger.warn("[CHAT_TITLE] Title generation failed chatId={} reason={}", chatId, ex.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
}
