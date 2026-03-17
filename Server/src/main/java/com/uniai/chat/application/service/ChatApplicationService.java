package com.uniai.chat.application.service;

import com.uniai.chat.application.dto.command.SendMessageCommand;
import com.uniai.chat.application.dto.response.ChatCreationResponseDto;
import com.uniai.chat.application.dto.response.MessageResponseDto;
import com.uniai.chat.application.port.in.*;
import com.uniai.chat.application.port.out.AiServicePort;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AiServicePort aiServicePort;

    // -------------------------------------------------------------------------
    // CreateChatUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public ChatCreationResponseDto createChat(String email) {
        User user = getUser(email);
        Chat chat = Chat.builder().user(user).build();
        chatRepository.save(chat);
        return ChatCreationResponseDto.builder().chatId(chat.getId()).build();
    }

    // -------------------------------------------------------------------------
    // SendMessageUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public MessageResponseDto sendMessage(String email, SendMessageCommand command) {
        validateContent(command.getContent());

        Chat chat = chatRepository.findById(command.getChatId())
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        User user = getUser(email);
        validateOwnership(chat, user);

        boolean isFirstMessage = (chat.getTitle() == null);

        Message userMessage = Message.builder()
                .chat(chat)
                .senderId(user.getId())
                .content(command.getContent())
                .timestamp(LocalDateTime.now())
                .build();
        messageRepository.save(userMessage);

        String aiContent = aiServicePort.generateResponse(command.getContent());
        Message aiMessage = Message.builder()
                .chat(chat)
                .senderId(0L)
                .content(aiContent)
                .timestamp(LocalDateTime.now())
                .build();
        messageRepository.save(aiMessage);

        if (isFirstMessage) {
            chat.setTitle(generateTitle(command.getContent()));
        }
        chat.setUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);

        return toDto(aiMessage);
    }

    // -------------------------------------------------------------------------
    // GetUserChatsUseCase
    // -------------------------------------------------------------------------

    @Override
    public List<Chat> getUserChats(String email) {
        User user = getUser(email);
        return chatRepository.findByUserUsernameOrderByUpdatedAtDesc(user.getUsername());
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
}
