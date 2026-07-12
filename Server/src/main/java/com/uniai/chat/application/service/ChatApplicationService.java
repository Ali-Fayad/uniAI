package com.uniai.chat.application.service;

import com.uniai.chat.application.dto.command.SendMessageCommand;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.budget.AiContextBudgetManager;
import com.uniai.chat.application.budget.AiContextBudgetResult;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetManager;
import com.uniai.chat.application.budget.GraduateQueryInterpretationBudgetResult;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretation;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationResult;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationStatus;
import com.uniai.chat.application.provider.AiProviderFailureCategory;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationValidator;
import com.uniai.chat.application.dto.response.ChatCreationResponseDto;
import com.uniai.chat.application.dto.response.ChatSummaryResponseDto;
import com.uniai.chat.application.dto.response.MessageResponseDto;
import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.ConversationMemoryManager;
import com.uniai.chat.application.title.ChatTitleGenerationManager;
import com.uniai.chat.application.port.in.*;
import com.uniai.chat.application.port.out.GraduateQueryInterpretationPort;
import com.uniai.chat.application.port.out.GraduateQueryInterpreterPromptPort;
import com.uniai.chat.application.port.out.GraduateKnowledgeRetrievalPort;
import com.uniai.chat.application.port.out.ChatSystemPromptPort;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.retrieval.GraduateFollowUpResolutionResult;
import com.uniai.chat.application.retrieval.GraduateFollowUpResolutionStatus;
import com.uniai.chat.application.retrieval.GraduateFollowUpResolver;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQueryInterpreter;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Application service for all chat use cases.
 */
@Service
@RequiredArgsConstructor
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
    private final GraduateQueryInterpretationPort graduateQueryInterpretationPort;
    private final GraduateQueryInterpreterPromptPort graduateQueryInterpreterPromptPort;
    private final GraduateQueryInterpretationBudgetManager graduateQueryInterpretationBudgetManager;
    private final GraduateQueryInterpretationValidator graduateQueryInterpretationValidator;
    private final GraduateKnowledgeRetrievalPort graduateKnowledgeRetrievalPort;
    private final UniversityCatalogRepository universityCatalogRepository;
    private final GraduateKnowledgeQueryInterpreter graduateKnowledgeQueryInterpreter;
    private final GraduateFollowUpResolver graduateFollowUpResolver;
    private final AiContextBudgetManager aiContextBudgetManager;
    private final ConversationMemoryManager conversationMemoryManager;
    private final ChatTitleGenerationManager chatTitleGenerationManager;

    private static final int MAX_CONVERSATION_HISTORY_MESSAGES = 6;
    private static final int MAX_INTERPRETATION_HISTORY_MESSAGES = 4;

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
            GraduateQueryInterpretationResult interpretationResult = interpretGraduateQuery(
                    chat.getId(),
                    command.getContent(),
                    recentConversationWindow,
                    universityCatalogs,
                    conversationMemory
            );
            logger.debug("[RETRIEVAL] Interpretation completed chatId={} status={} fallbackUsed={} resolvedUniversityCount={} degreeTypeCount={} ambiguous={} failureCategory={}",
                    chat.getId(),
                    interpretationResult.status(),
                    interpretationResult.fallbackUsed(),
                    interpretationResult.resolvedUniversityCount(),
                    interpretationResult.degreeTypeCount(),
                    interpretationResult.ambiguous(),
                    interpretationResult.failureCategory());

            if (interpretationResult.status() == GraduateQueryInterpretationStatus.AMBIGUOUS
                    || interpretationResult.status() == GraduateQueryInterpretationStatus.UNSUPPORTED) {
                String safeContent = StringUtils.hasText(interpretationResult.safeMessage())
                        ? interpretationResult.safeMessage()
                        : buildSafeInterpretationMessage(interpretationResult.status());
                logger.info("[CHAT] Interpretation stopped before retrieval chatId={} status={} reason={}",
                        chat.getId(),
                        interpretationResult.status(),
                        interpretationResult.failureCategory());
                Message aiMessage = MessageBuilder.aiMessage(chat, safeContent).build();
                long aiSaveStartNanos = System.nanoTime();
                Message persistedAiMessage = messageRepository.save(aiMessage);
                logger.debug("[PERSISTENCE] Assistant message saved id={} chatId={} durationMs={}",
                        persistedAiMessage != null ? persistedAiMessage.getId() : null,
                        chat.getId(),
                        elapsedMillis(aiSaveStartNanos));

                chat.setUpdatedAt(LocalDateTime.now());
                chatRepository.save(chat);
                registerChatTitleGeneration(chat.getId(), command.getContent(), isFirstUserTurn);
                registerConversationMemoryUpdate(chat.getId(), conversationMemory, command.getContent(), safeContent, interpretationResult);

                logger.info("[CHAT] Request completed userId={} chatId={} assistantMessageId={} responseLength={} durationMs={}",
                        user.getId(),
                        chat.getId(),
                        persistedAiMessage != null ? persistedAiMessage.getId() : null,
                        safeContent.length(),
                        elapsedMillis(requestStartNanos));

                return toDto(aiMessage);
            }

            logger.debug("[RETRIEVAL] Retrieval started chatId={} historyWindowCount={}",
                    chat.getId(),
                    recentConversationWindow.size());
            long retrievalStartNanos = System.nanoTime();
            GraduateKnowledgeQuery graduateKnowledgeQuery = interpretationResult.query();
            if (graduateKnowledgeQuery == null) {
                graduateKnowledgeQuery = graduateKnowledgeQueryInterpreter.interpret(
                        command.getContent(),
                        recentConversationWindow,
                        universityCatalogs
                );
            }
            logger.debug("[RETRIEVAL] Query interpreted chatId={} intent={} universityCount={} degreeTypeCount={} followUpResolved={} ambiguous={} detailLevel={}",
                    chat.getId(),
                    graduateKnowledgeQuery.intent(),
                    graduateKnowledgeQuery.resolvedUniversities().size(),
                    graduateKnowledgeQuery.degreeTypes().size(),
                    graduateKnowledgeQuery.followUpResolved(),
                    graduateKnowledgeQuery.ambiguous(),
                    graduateKnowledgeQuery.detailLevel());
            String graduateContext = graduateKnowledgeRetrievalPort.retrieveContext(graduateKnowledgeQuery);
            long retrievalDurationMs = elapsedMillis(retrievalStartNanos);
            logger.debug("[RETRIEVAL] Retrieval completed chatId={} contextLength={} durationMs={}",
                    chat.getId(),
                    graduateContext != null ? graduateContext.length() : 0,
                    retrievalDurationMs);
            List<String> context = (graduateContext != null && !graduateContext.isBlank())
                    ? List.of(graduateContext)
                    : Collections.emptyList();
            if (context.isEmpty()) {
                logger.warn("[RETRIEVAL] Empty context returned chatId={}", chat.getId());
            }

            String systemPrompt = chatSystemPromptPort.getPrompt();
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
            if (!budgetResult.requestFits()) {
                budgetRejected = true;
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
                AiRequest budgetedRequest = budgetResult.request();
                logger.debug("[AI] Provider invocation started chatId={} providerBean={} historyCount={} contextCount={} maxTokens={}",
                        chat.getId(),
                        aiServicePort.getClass().getSimpleName(),
                        budgetedRequest.getConversationHistory() != null ? budgetedRequest.getConversationHistory().size() : 0,
                        budgetedRequest.getContext() != null ? budgetedRequest.getContext().size() : 0,
                        budgetedRequest.getMaxTokens());
                long providerStartNanos = System.nanoTime();
                aiResponse = aiServicePort.generateResponse(budgetedRequest);
                providerDurationMs = elapsedMillis(providerStartNanos);
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
                    logger.warn("[AI] Provider fallback used provider={} model={} failureCategory={} retryable={} chatId={} durationMs={}",
                            aiResponse.getProvider(),
                            aiResponse.getModel(),
                            aiResponse.getFailureCategory(),
                            aiResponse.getRetryable(),
                            chat.getId(),
                            providerDurationMs);
                } else {
                    logger.debug("[AI] Provider success provider={} model={} finishReason={} responseLength={} chatId={} durationMs={}",
                            aiResponse.getProvider(),
                            aiResponse.getModel(),
                            aiResponse.getFinishReason(),
                            aiContent.length(),
                            chat.getId(),
                            providerDurationMs);
                }
            }
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
            registerConversationMemoryUpdate(chat.getId(), conversationMemory, command.getContent(), aiContent, interpretationResult);

            logger.info("[CHAT] Request completed userId={} chatId={} assistantMessageId={} responseLength={} durationMs={}",
                    user.getId(),
                    chat.getId(),
                    persistedAiMessage != null ? persistedAiMessage.getId() : null,
                    aiContent.length(),
                    elapsedMillis(requestStartNanos));

            return toDto(aiMessage);
        } catch (RuntimeException ex) {
            logger.error("[CHAT] Request failed userId={} chatId={} durationMs={} reason={}",
                    userId,
                    chatId,
                    elapsedMillis(requestStartNanos),
                    ex.getMessage(),
                    ex);
            throw ex;
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
            history.add(AiConversationMessage.builder()
                    .role(resolveConversationRole(message))
                    .content(message.getContent())
                    .build());
        }
        return history;
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private List<AiConversationMessage> buildRecentConversationWindow(List<AiConversationMessage> conversationHistory) {
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            return Collections.emptyList();
        }

        int startIndex = Math.max(0, conversationHistory.size() - MAX_INTERPRETATION_HISTORY_MESSAGES);
        return List.copyOf(conversationHistory.subList(startIndex, conversationHistory.size()));
    }

    private GraduateQueryInterpretationResult interpretGraduateQuery(
            Long chatId,
            String currentMessage,
            List<AiConversationMessage> recentConversationWindow,
            List<UniversityCatalog> universityCatalogs,
            ConversationMemory conversationMemory
    ) {
        String prompt = graduateQueryInterpreterPromptPort.getPrompt();
        GraduateQueryInterpretationRequest request = new GraduateQueryInterpretationRequest(currentMessage, recentConversationWindow, conversationMemory);
        GraduateQueryInterpretationBudgetResult budgetResult = graduateQueryInterpretationBudgetManager.budget(request, prompt);

        logger.debug("[AI_INTERPRETATION] Budget evaluation chatId={} provider={} requestFits={} originalEstimatedInputTokens={} finalEstimatedInputTokens={} historyTrimmed={} finalHistoryCount={} reservedOutputTokens={}",
                chatId,
                aiServicePort.getClass().getSimpleName(),
                budgetResult.requestFits(),
                budgetResult.originalEstimatedInputTokens(),
                budgetResult.finalEstimatedInputTokens(),
                budgetResult.historyTrimmed(),
                budgetResult.finalHistoryCount(),
                budgetResult.reservedOutputTokens());

        if (!budgetResult.requestFits()) {
            logger.warn("[AI_INTERPRETATION] Budget rejected chatId={} category={}",
                    chatId,
                    budgetResult.diagnosticCategory());
            GraduateQueryInterpretationResult fallback = fallbackInterpretation(currentMessage, recentConversationWindow, universityCatalogs, conversationMemory, "AI_QUERY_INTERPRETATION_BUDGET_REJECTED");
            return resolveFollowUpInterpretation(currentMessage, recentConversationWindow, universityCatalogs, conversationMemory, fallback, "AI_QUERY_INTERPRETATION_BUDGET_REJECTED");
        }

        try {
            GraduateQueryInterpretation rawInterpretation = graduateQueryInterpretationPort.interpret(budgetResult.request());
            GraduateQueryInterpretationResult result = graduateQueryInterpretationValidator.validate(rawInterpretation, universityCatalogs);
            GraduateQueryInterpretationResult resolved = resolveFollowUpInterpretation(currentMessage, recentConversationWindow, universityCatalogs, conversationMemory, result, null);
            logger.debug("[AI_INTERPRETATION] Validation completed chatId={} status={} resolvedUniversityCount={} degreeTypeCount={} ambiguous={}",
                    chatId,
                    resolved.status(),
                    resolved.resolvedUniversityCount(),
                    resolved.degreeTypeCount(),
                    resolved.ambiguous());
            return resolved;
        } catch (RuntimeException ex) {
            logger.warn("[AI_INTERPRETATION] Provider interpretation failed chatId={} reason={}", chatId, ex.getMessage());
            GraduateQueryInterpretationResult fallback = fallbackInterpretation(currentMessage, recentConversationWindow, universityCatalogs, conversationMemory, "AI_QUERY_INTERPRETATION_PROVIDER_FAILURE");
            return resolveFollowUpInterpretation(currentMessage, recentConversationWindow, universityCatalogs, conversationMemory, fallback, "AI_QUERY_INTERPRETATION_PROVIDER_FAILURE");
        }
    }

    private GraduateQueryInterpretationResult fallbackInterpretation(
            String currentMessage,
            List<AiConversationMessage> recentConversationWindow,
            List<UniversityCatalog> universityCatalogs,
            ConversationMemory conversationMemory,
            String failureCategory
    ) {
        if (isUnsupportedGraduateDegreeRequest(currentMessage)) {
            return GraduateQueryInterpretationResult.unsupported(
                    buildUnsupportedGraduateMessage(),
                    0,
                    0,
                    List.of("BACHELOR", "UNDERGRADUATE")
            );
        }

        GraduateKnowledgeQuery fallbackQuery = graduateKnowledgeQueryInterpreter.interpret(
                currentMessage,
                recentConversationWindow,
                universityCatalogs,
                conversationMemory
        );

        if (fallbackQuery.intent() == null || fallbackQuery.intent() == com.uniai.chat.application.retrieval.GraduateKnowledgeIntent.UNKNOWN_OR_AMBIGUOUS
                || fallbackQuery.ambiguous()
                || fallbackQuery.resolvedUniversities().isEmpty()) {
            return GraduateQueryInterpretationResult.ambiguous(
                    buildAmbiguousGraduateMessage(),
                    fallbackQuery.resolvedUniversities().size(),
                    fallbackQuery.degreeTypes().size(),
                    fallbackQuery
            );
        }

        return GraduateQueryInterpretationResult.fallbackUsed(fallbackQuery, buildFallbackUsedMessage(failureCategory));
    }

    private GraduateQueryInterpretationResult resolveFollowUpInterpretation(
            String currentMessage,
            List<AiConversationMessage> recentConversationWindow,
            List<UniversityCatalog> universityCatalogs,
            ConversationMemory conversationMemory,
            GraduateQueryInterpretationResult interpretationResult,
            String fallbackFailureCategory
    ) {
        if (interpretationResult == null
                || interpretationResult.status() == GraduateQueryInterpretationStatus.UNSUPPORTED
                || interpretationResult.status() == GraduateQueryInterpretationStatus.INVALID) {
            return interpretationResult;
        }

        GraduateKnowledgeQuery candidateQuery = interpretationResult.query();
        if (candidateQuery == null || graduateFollowUpResolver == null) {
            return interpretationResult;
        }

        GraduateFollowUpResolutionResult resolution = graduateFollowUpResolver.resolve(
                currentMessage,
                candidateQuery,
                recentConversationWindow,
                conversationMemory,
                universityCatalogs
        );

        if (resolution.status() == GraduateFollowUpResolutionStatus.CLARIFICATION_REQUIRED) {
            GraduateKnowledgeQuery clarificationQuery = resolution.resolvedQuery() != null ? resolution.resolvedQuery() : candidateQuery;
            return GraduateQueryInterpretationResult.ambiguous(
                    buildClarificationMessage(resolution.clarificationReason()),
                    clarificationQuery.resolvedUniversities().size(),
                    clarificationQuery.degreeTypes().size(),
                    clarificationQuery
            );
        }

        if (resolution.status() == GraduateFollowUpResolutionStatus.UNSUPPORTED) {
            return GraduateQueryInterpretationResult.unsupported(
                    buildUnsupportedGraduateMessage(),
                    candidateQuery.resolvedUniversities().size(),
                    candidateQuery.degreeTypes().size(),
                    List.of()
            );
        }

        GraduateKnowledgeQuery resolvedQuery = resolution.resolvedQuery() != null ? resolution.resolvedQuery() : candidateQuery;
        if (interpretationResult.status() == GraduateQueryInterpretationStatus.FALLBACK_USED) {
            return GraduateQueryInterpretationResult.fallbackUsed(resolvedQuery, buildFallbackUsedMessage(fallbackFailureCategory));
        }
        return GraduateQueryInterpretationResult.valid(
                resolvedQuery,
                resolvedQuery.resolvedUniversities().size(),
                resolvedQuery.degreeTypes().size()
        );
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

    private String buildSafeInterpretationMessage(GraduateQueryInterpretationStatus status) {
        if (status == GraduateQueryInterpretationStatus.UNSUPPORTED) {
            return buildUnsupportedGraduateMessage();
        }
        return buildAmbiguousGraduateMessage();
    }

    private void registerConversationMemoryUpdate(
            Long chatId,
            ConversationMemory previousMemory,
            String currentUserMessage,
            String assistantResponse,
            GraduateQueryInterpretationResult interpretationResult
    ) {
        if (conversationMemoryManager == null || chatId == null) {
            return;
        }

        Runnable task = () -> {
            try {
                conversationMemoryManager.updateMemoryIfNeeded(chatId, previousMemory, currentUserMessage, assistantResponse, interpretationResult);
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
        return MessageResponseDto.builder()
                .messageId(message.getId())
                .chatId(message.getChatId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
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
