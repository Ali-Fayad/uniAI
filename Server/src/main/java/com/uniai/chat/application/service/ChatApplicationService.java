package com.uniai.chat.application.service;

import com.uniai.chat.application.dto.command.SendMessageCommand;
import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.dto.response.ChatCreationResponseDto;
import com.uniai.chat.application.dto.response.ChatSummaryResponseDto;
import com.uniai.chat.application.dto.response.MessageResponseDto;
import com.uniai.chat.application.port.in.*;
import com.uniai.chat.application.port.out.GraduateKnowledgeRetrievalPort;
import com.uniai.chat.application.port.out.ChatSystemPromptPort;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.domain.builder.ChatBuilder;
import com.uniai.chat.domain.builder.MessageBuilder;
import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.model.Message;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.domain.repository.MessageRepository;
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
    private final GraduateKnowledgeRetrievalPort graduateKnowledgeRetrievalPort;

    private static final int MAX_CONVERSATION_HISTORY_MESSAGES = 20;

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

            logger.info("[CHAT] Request started userId={} chatId={} messageLength={}",
                    user.getId(),
                    chat.getId(),
                    command.getContent() != null ? command.getContent().length() : 0);

            boolean isFirstMessage = (chat.getTitle() == null);

            Message userMessage = MessageBuilder.userMessage(chat, user.getId(), command.getContent()).build();
            long userSaveStartNanos = System.nanoTime();
            Message persistedUserMessage = messageRepository.save(userMessage);
            logger.debug("[PERSISTENCE] User message saved id={} chatId={} durationMs={}",
                    persistedUserMessage != null ? persistedUserMessage.getId() : null,
                    chat.getId(),
                    elapsedMillis(userSaveStartNanos));

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
            logger.debug("[RETRIEVAL] Retrieval started chatId={} historyWindowCount={}",
                    chat.getId(),
                    recentConversationWindow.size());
            long retrievalStartNanos = System.nanoTime();
            String graduateContext = graduateKnowledgeRetrievalPort.retrieveContext(
                    command.getContent(),
                    recentConversationWindow
            );
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
                    .build();

            logger.debug("[AI] Provider invocation started chatId={} providerBean={} historyCount={} contextCount={}",
                    chat.getId(),
                    aiServicePort.getClass().getSimpleName(),
                    conversationHistory.size(),
                    context.size());
            long providerStartNanos = System.nanoTime();
            AiResponse aiResponse = aiServicePort.generateResponse(aiRequest);
            long providerDurationMs = elapsedMillis(providerStartNanos);
            String aiContent = (aiResponse != null && aiResponse.getContent() != null)
                    ? aiResponse.getContent()
                    : "AI service error : this message is from ChatApplicationService. Please try again later.";
            if (aiResponse == null) {
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
                    logger.warn("[AI] Provider fallback used provider={} model={} chatId={} durationMs={}",
                            aiResponse.getProvider(),
                            aiResponse.getModel(),
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

            if (isFirstMessage) {
                chat.setTitle(generateTitle(command.getContent()));
            }
            chat.setUpdatedAt(LocalDateTime.now());
            chatRepository.save(chat);

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

        int startIndex = Math.max(0, conversationHistory.size() - 6);
        return List.copyOf(conversationHistory.subList(startIndex, conversationHistory.size()));
    }

    private String resolveConversationRole(Message message) {
        if (message == null || message.getSenderId() == null) {
            return "user";
        }
        return message.getSenderId() == 0L ? "assistant" : "user";
    }

    private String generateTitle(String firstMessage) {
        int maxLen = Math.min(30, firstMessage.length());
        return firstMessage.substring(0, maxLen) + (firstMessage.length() > 30 ? "..." : "");
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
}
