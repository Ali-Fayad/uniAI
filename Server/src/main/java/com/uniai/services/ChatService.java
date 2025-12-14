package com.uniai.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uniai.builder.ChatBuilder;
import com.uniai.dto.ChatCreationResponseDto;
import com.uniai.dto.MessageResponseDto;
import com.uniai.dto.SendMessageDto;
import com.uniai.exception.ChatNotFoundException;
import com.uniai.exception.EmailNotFoundException;
import com.uniai.exception.UnauthorizedAccessException;
import com.uniai.exception.InvalidMessageException;
import com.uniai.model.Chat;
import com.uniai.model.Message;
import com.uniai.model.User;
import com.uniai.repository.ChatRepository;
import com.uniai.repository.MessageRepository;
import com.uniai.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * Create a new chat (title is NULL initially).
     * Title will be generated when first message arrives.
     */
    @Transactional
    public ChatCreationResponseDto createChat(String email) {
        User user = validateAndGetUser(email);

        Chat chat = Chat.builder()
                .user(user)
                .build();

        chatRepository.save(chat);

        return ChatBuilder.toChatCreationResponse(chat);
    }

    /**
     * Send a user message and get AI response.
     * ⚡ Title generation happens ONLY if this is the first message.
     * Returns only the AI response (frontend already shows user message).
     * Time complexity: O(1)
     */
    @Transactional
    public MessageResponseDto sendMessage(String email, SendMessageDto dto) {

        validateSendMessageDto(dto);

        Chat chat = chatRepository.findById(dto.getChatId())
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        User user = validateAndGetUser(email);
        validateChatOwnership(chat, user);

        boolean isFirstMessage = (chat.getTitle() == null);

        Message userMessage = ChatBuilder.buildUserMessage(dto, user.getId());
        messageRepository.save(userMessage);

        String aiResponseContent = generateAIResponse(dto.getContent());

        Message aiMessage = ChatBuilder.buildAIMessage(dto.getChatId(), aiResponseContent);
        messageRepository.save(aiMessage);

        if (isFirstMessage)
            chat.setTitle(generateChatTitle(userMessage.getContent()));
        chat.setUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);

        return ChatBuilder.toMessageResponse(aiMessage);
    }

    /**
     * Delete a specific chat and all its messages.
     * Cascade delete: all messages in the chat will be deleted.
     */
    @Transactional
    public void deleteChat(String email, Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        User user = validateAndGetUser(email);
        validateChatOwnership(chat, user);

        messageRepository.deleteByChatId(chatId);

        chatRepository.delete(chat);
    }

    /**
     * Delete all chats for the authenticated user.
     * Cascade delete: all messages in all user's chats will be deleted.
     */
    @Transactional
    public void deleteAllChats(String email) {
        User user = validateAndGetUser(email);

        List<Chat> userChats = chatRepository.findByUserUsernameOrderByUpdatedAtDesc(user.getUsername());

        if (userChats.isEmpty()) {
            return;
        }

        List<Long> chatIds = userChats.stream()
                .map(Chat::getId)
                .toList();

        messageRepository.deleteByChatIdIn(chatIds);

        chatRepository.deleteAll(userChats);
    }

    /**
     * Get all messages in a chat (for loading history).
     */
    public List<MessageResponseDto> getChatMessages(String email, Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        User user = validateAndGetUser(email);
        validateChatOwnership(chat, user);

        List<Message> messages = messageRepository.findByChatIdOrderByTimestampAsc(chatId);
        return messages.stream()
                .map(ChatBuilder::toMessageResponse)
                .toList();
    }

    /**
     * Get all user's chats (with titles).
     */
    public List<Chat> getUserChats(String email) {
        User user = validateAndGetUser(email);
        return chatRepository.findByUserUsernameOrderByUpdatedAtDesc(user.getUsername());
    }

    /**
     * Validate and retrieve user by email.
     * Throws EmailNotFoundException if user doesn't exist.
     */
    private User validateAndGetUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new EmailNotFoundException();
        }
        return user;
    }

    /**
     * Validate that the user owns the chat.
     * Throws UnauthorizedAccessException if user doesn't own the chat.
     */
    private void validateChatOwnership(Chat chat, User user) {
        if (!chat.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to access this chat");
        }
    }

    /**
     * Validate SendMessageDto fields.
     * Throws InvalidMessageException if validation fails.
     */
    private void validateSendMessageDto(SendMessageDto dto) {
        if (dto.getChatId() == null) {
            throw new InvalidMessageException("Chat ID cannot be null");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new InvalidMessageException("Message content cannot be empty");
        }
        if (dto.getContent().length() > 5000) {
            throw new InvalidMessageException("Message content is too long (max 5000 characters)");
        }
    }

    private String generateAIResponse(String userContent) {

        return "AI response to:  " + userContent;
    }

    /**
     * Generate chat title from first message.
     * Later: replace with AI-powered title generation.
     */
    private String generateChatTitle(String firstMessageContent) {
        int maxLen = Math.min(30, firstMessageContent.length());
        return firstMessageContent.substring(0, maxLen) + (firstMessageContent.length() > 30 ? "..." : "");
    }
}