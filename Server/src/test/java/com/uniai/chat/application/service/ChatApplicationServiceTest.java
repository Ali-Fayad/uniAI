package com.uniai.chat.application.service;

import com.uniai.chat.application.dto.ai.AiConversationMessage;
import com.uniai.chat.application.budget.AiContextBudgetConfiguration;
import com.uniai.chat.application.budget.AiContextBudgetManager;
import com.uniai.chat.application.budget.AiTokenEstimator;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.dto.command.SendMessageCommand;
import com.uniai.chat.application.dto.response.MessageResponseDto;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.port.out.ChatSystemPromptPort;
import com.uniai.chat.application.port.out.GraduateKnowledgeRetrievalPort;
import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.model.Message;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.domain.repository.MessageRepository;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatApplicationServiceTest {

    private InMemoryChatRepository chatRepository;
    private InMemoryMessageRepository messageRepository;
    private InMemoryUserRepository userRepository;
    private RecordingAiServicePort aiServicePort;
    private FixedChatSystemPromptPort chatSystemPromptPort;
    private RecordingGraduateKnowledgeRetrievalPort graduateKnowledgeRetrievalPort;
    private AiContextBudgetManager aiContextBudgetManager;
    private ChatApplicationService chatApplicationService;

    @BeforeEach
    void setUp() {
        chatRepository = new InMemoryChatRepository();
        messageRepository = new InMemoryMessageRepository();
        userRepository = new InMemoryUserRepository();
        aiServicePort = new RecordingAiServicePort();
        chatSystemPromptPort = new FixedChatSystemPromptPort("Static uniAI system prompt");
        graduateKnowledgeRetrievalPort = new RecordingGraduateKnowledgeRetrievalPort("Structured graduate context");
        aiContextBudgetManager = budgetManager("gemini", 200000, 2000, 12000, 120000, 4, 128);

        chatApplicationService = new ChatApplicationService(
                chatRepository,
                messageRepository,
                userRepository,
                aiServicePort,
                chatSystemPromptPort,
                graduateKnowledgeRetrievalPort,
                aiContextBudgetManager
        );
    }

    @Test
    void sendMessageShouldBuildAiRequestWithSystemPromptHistoryContextAndCurrentMessage() {
        User user = user(1L, "alice", "alice@example.com");
        Chat chat = chat(10L, user, "chat-title");
        userRepository.save(user);
        chatRepository.save(chat);
        seedChatMessages(chat, user, 42L, 7);
        SendMessageCommand command = SendMessageCommand.builder()
                .chatId(chat.getId())
                .content("What about USJ?")
                .build();

        MessageResponseDto result = chatApplicationService.sendMessage(user.getEmail(), command);

        assertEquals("Static uniAI system prompt", aiServicePort.lastRequest.getSystemPrompt());
        assertEquals("What about USJ?", aiServicePort.lastRequest.getUserMessage());
        assertEquals("Structured graduate context", aiServicePort.lastRequest.getContext().get(0));
        assertEquals(2000, aiServicePort.lastRequest.getMaxTokens());
        assertEquals(7, aiServicePort.lastRequest.getConversationHistory().size());
        assertFalse(aiServicePort.lastRequest.getConversationHistory().stream()
                .anyMatch(message -> "What about USJ?".equals(message.getContent())));
        assertEquals(List.of(
                "What master's programs does AUB offer?",
                "AUB master's answer",
                "What about tuition?",
                "AUB tuition answer",
                "And admission?",
                "AUB admission answer",
                "Any scholarships?"
        ), aiServicePort.lastRequest.getConversationHistory().stream().map(AiConversationMessage::getContent).toList());

        assertEquals(1, graduateKnowledgeRetrievalPort.callCount);
        assertEquals("What about USJ?", graduateKnowledgeRetrievalPort.lastUserMessage);
        assertEquals(6, graduateKnowledgeRetrievalPort.lastRecentConversationWindow.size());
        assertEquals(List.of(
                "AUB master's answer",
                "What about tuition?",
                "AUB tuition answer",
                "And admission?",
                "AUB admission answer",
                "Any scholarships?"
        ), graduateKnowledgeRetrievalPort.lastRecentConversationWindow.stream().map(AiConversationMessage::getContent).toList());
        assertFalse(graduateKnowledgeRetrievalPort.lastRecentConversationWindow.stream()
                .anyMatch(message -> "Other chat message".equals(message.getContent())));

        assertEquals("Here is the official answer.", result.getContent());
        assertEquals("Here is the official answer.", messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).get(messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).size() - 1).getContent());
        assertEquals(1, aiServicePort.callCount);
        assertEquals(4, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).stream()
                .filter(message -> message.getSenderId() != null && message.getSenderId() == 0L)
                .count());
        assertEquals("What about USJ?", aiServicePort.lastRequest.getUserMessage());
        assertEquals("Structured graduate context", aiServicePort.lastRequest.getContext().get(0));
    }

    @Test
    void sendMessageShouldSaveSafeFallbackContentWhenAiResponseIsFallbackWithoutContent() {
        User user = user(2L, "bob", "bob@example.com");
        Chat chat = chat(20L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);
        aiServicePort.nextResponse = AiResponse.builder()
                .fallback(true)
                .content(null)
                .provider("gemini")
                .model("gemini-2.5-flash")
                .build();

        MessageResponseDto result = chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("Hello").build()
        );

        assertEquals("AI service error : this message is from ChatApplicationService. Please try again later.", result.getContent());
        List<Message> messages = messageRepository.findByChatIdOrderByTimestampAsc(chat.getId());
        assertEquals(2, messages.size());
        assertEquals("AI service error : this message is from ChatApplicationService. Please try again later.", messages.get(1).getContent());
        assertTrue(aiServicePort.lastRequest.getConversationHistory().isEmpty());
        assertEquals("Static uniAI system prompt", aiServicePort.lastRequest.getSystemPrompt());
    }

    @Test
    void sendMessageShouldKeepHistoryLocalToTheRequestedChat() {
        User user = user(3L, "carol", "carol@example.com");
        Chat primaryChat = chat(30L, user, null);
        Chat otherChat = chat(31L, user, null);
        userRepository.save(user);
        chatRepository.save(primaryChat);
        chatRepository.save(otherChat);

        seedChatMessages(primaryChat, user, 100L, 3);
        seedOtherChatMessages(otherChat, user);

        chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(primaryChat.getId()).content("Tell me more").build()
        );

        assertFalse(aiServicePort.lastRequest.getConversationHistory().stream()
                .anyMatch(message -> message.getContent().contains("Other chat")));
        assertFalse(graduateKnowledgeRetrievalPort.lastRecentConversationWindow.stream()
                .anyMatch(message -> message.getContent().contains("Other chat")));
    }

    @Test
    void sendMessageShouldSkipProviderWhenBudgetCannotFitRequiredContent() {
        AiContextBudgetManager smallBudgetManager = budgetManager("gemini", 12, 10, 10, 10, 4, 0);
        ChatApplicationService budgetLimitedService = new ChatApplicationService(
                chatRepository,
                messageRepository,
                userRepository,
                aiServicePort,
                chatSystemPromptPort,
                graduateKnowledgeRetrievalPort,
                smallBudgetManager
        );

        User user = user(4L, "dan", "dan@example.com");
        Chat chat = chat(40L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);

        MessageResponseDto result = budgetLimitedService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("Hello").build()
        );

        assertEquals(0, aiServicePort.callCount);
        assertEquals("AI service error : this message is from ChatApplicationService. Please try again later.", result.getContent());
        List<Message> messages = messageRepository.findByChatIdOrderByTimestampAsc(chat.getId());
        assertEquals(2, messages.size());
        assertEquals("AI service error : this message is from ChatApplicationService. Please try again later.", messages.get(1).getContent());
        assertEquals(1, messages.stream()
                .filter(message -> message.getSenderId() != null && message.getSenderId() == 0L)
                .count());
    }

    @Test
    void sendMessageShouldHandleProviderFailuresSeparatelyFromBudgetRejection() {
        User user = user(5L, "erin", "erin@example.com");
        Chat chat = chat(50L, user, null);
        userRepository.save(user);
        chatRepository.save(chat);
        aiServicePort.nextRuntimeException = new IllegalStateException("provider unavailable");

        assertThrows(IllegalStateException.class, () -> chatApplicationService.sendMessage(
                user.getEmail(),
                SendMessageCommand.builder().chatId(chat.getId()).content("Hello").build()
        ));

        assertEquals(1, aiServicePort.callCount);
        assertEquals(1, messageRepository.findByChatIdOrderByTimestampAsc(chat.getId()).size());
    }

    private void seedChatMessages(Chat chat, User user, long baseMinutesAgo, int existingMessageCount) {
        List<String> contents = List.of(
                "What master's programs does AUB offer?",
                "AUB master's answer",
                "What about tuition?",
                "AUB tuition answer",
                "And admission?",
                "AUB admission answer",
                "Any scholarships?"
        );

        for (int i = 0; i < existingMessageCount; i++) {
            Message message = Message.builder()
                    .chat(chat)
                    .senderId(i % 2 == 0 ? user.getId() : 0L)
                    .content(contents.get(i))
                    .timestamp(LocalDateTime.now().minusMinutes(baseMinutesAgo - i))
                    .build();
            messageRepository.save(message);
        }
    }

    private void seedOtherChatMessages(Chat otherChat, User user) {
        messageRepository.save(Message.builder()
                .chat(otherChat)
                .senderId(user.getId())
                .content("Other chat message")
                .timestamp(LocalDateTime.now().minusMinutes(1))
                .build());
    }

    private User user(Long id, String username, String email) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .password("Password123!")
                .build();
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
        AiContextBudgetConfiguration configuration = new AiContextBudgetConfiguration(
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

    private Chat chat(Long id, User user, String title) {
        return Chat.builder()
                .id(id)
                .user(user)
                .title(title)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusMinutes(1))
                .build();
    }

    private static final class RecordingAiServicePort implements AiServicePort {
        private AiRequest lastRequest;
        private int callCount;
        private RuntimeException nextRuntimeException;
        private AiResponse nextResponse = AiResponse.builder()
                .content("Here is the official answer.")
                .provider("gemini")
                .model("gemini-2.5-flash")
                .build();

        @Override
        public AiResponse generateResponse(AiRequest request) {
            callCount++;
            lastRequest = request;
            if (nextRuntimeException != null) {
                throw nextRuntimeException;
            }
            return nextResponse;
        }
    }

    private static final class FixedChatSystemPromptPort implements ChatSystemPromptPort {
        private final String prompt;

        private FixedChatSystemPromptPort(String prompt) {
            this.prompt = prompt;
        }

        @Override
        public String getPrompt() {
            return prompt;
        }
    }

    private static final class RecordingGraduateKnowledgeRetrievalPort implements GraduateKnowledgeRetrievalPort {
        private final String context;
        private int callCount;
        private String lastUserMessage;
        private List<AiConversationMessage> lastRecentConversationWindow = Collections.emptyList();

        private RecordingGraduateKnowledgeRetrievalPort(String context) {
            this.context = context;
        }

        @Override
        public String retrieveContext(String userMessage, List<AiConversationMessage> recentConversationHistory) {
            callCount++;
            lastUserMessage = userMessage;
            lastRecentConversationWindow = recentConversationHistory == null
                    ? Collections.emptyList()
                    : List.copyOf(recentConversationHistory);
            return context;
        }
    }

    private static final class InMemoryUserRepository implements UserRepository {
        private final Map<Long, User> byId = new LinkedHashMap<>();
        private final Map<String, User> byEmail = new LinkedHashMap<>();

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(byEmail.get(email));
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return byId.values().stream()
                    .filter(user -> username != null && username.equals(user.getUsername()))
                    .findFirst();
        }

        @Override
        public boolean existsByEmail(String email) {
            return byEmail.containsKey(email);
        }

        @Override
        public boolean existsByUsername(String username) {
            return byId.values().stream().anyMatch(user -> username != null && username.equals(user.getUsername()));
        }

        @Override
        public User save(User user) {
            byId.put(user.getId(), user);
            if (user.getEmail() != null) {
                byEmail.put(user.getEmail(), user);
            }
            return user;
        }

        @Override
        public void delete(User user) {
            if (user != null) {
                byId.remove(user.getId());
                if (user.getEmail() != null) {
                    byEmail.remove(user.getEmail());
                }
            }
        }

        @Override
        public boolean deleteByEmail(String email) {
            return byEmail.remove(email) != null;
        }

        @Override
        public boolean deleteByUsername(String username) {
            Long id = byId.values().stream()
                    .filter(user -> username != null && username.equals(user.getUsername()))
                    .map(User::getId)
                    .findFirst()
                    .orElse(null);
            if (id == null) {
                return false;
            }
            User removed = byId.remove(id);
            if (removed != null && removed.getEmail() != null) {
                byEmail.remove(removed.getEmail());
            }
            return true;
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(byId.values());
        }

        @Override
        public List<User> searchByEmail(String email) {
            return byEmail.values().stream()
                    .filter(user -> email != null && user.getEmail() != null && user.getEmail().contains(email))
                    .toList();
        }

        @Override
        public long count() {
            return byId.size();
        }

        @Override
        public long countByRole(com.uniai.user.domain.valueobject.UserRole role) {
            return byId.values().stream().filter(user -> user.getRole() == role).count();
        }
    }

    private static final class InMemoryChatRepository implements ChatRepository {
        private final Map<Long, Chat> chats = new LinkedHashMap<>();

        @Override
        public Optional<Chat> findById(Long id) {
            return Optional.ofNullable(chats.get(id));
        }

        @Override
        public List<Chat> findByUserUsernameOrderByUpdatedAtDesc(String username) {
            return chats.values().stream()
                    .filter(chat -> chat.getUser() != null && username != null && username.equals(chat.getUser().getUsername()))
                    .sorted((left, right) -> {
                        if (left.getUpdatedAt() == null && right.getUpdatedAt() == null) return 0;
                        if (left.getUpdatedAt() == null) return 1;
                        if (right.getUpdatedAt() == null) return -1;
                        return right.getUpdatedAt().compareTo(left.getUpdatedAt());
                    })
                    .toList();
        }

        @Override
        public String findTitleById(Long chatId) {
            Chat chat = chats.get(chatId);
            return chat != null ? chat.getTitle() : null;
        }

        @Override
        public Chat save(Chat chat) {
            chats.put(chat.getId(), chat);
            return chat;
        }

        @Override
        public void delete(Chat chat) {
            if (chat != null) {
                chats.remove(chat.getId());
            }
        }

        @Override
        public void deleteAll(List<Chat> chats) {
            if (chats != null) {
                chats.forEach(chat -> {
                    if (chat != null) {
                        this.chats.remove(chat.getId());
                    }
                });
            }
        }

        @Override
        public long count() {
            return chats.size();
        }

        @Override
        public long countByUserId(Long userId) {
            return chats.values().stream()
                    .filter(chat -> chat.getUser() != null && chat.getUser().getId() != null && chat.getUser().getId().equals(userId))
                    .count();
        }
    }

    private static final class InMemoryMessageRepository implements MessageRepository {
        private final Map<Long, List<Message>> messagesByChatId = new LinkedHashMap<>();

        @Override
        public List<Message> findByChatIdOrderByTimestampAsc(Long chatId) {
            return messagesByChatId.getOrDefault(chatId, List.of()).stream()
                    .sorted((left, right) -> {
                        if (left.getTimestamp() == null && right.getTimestamp() == null) return 0;
                        if (left.getTimestamp() == null) return 1;
                        if (right.getTimestamp() == null) return -1;
                        return left.getTimestamp().compareTo(right.getTimestamp());
                    })
                    .toList();
        }

        @Override
        public List<Message> findTop10ByChatIdOrderByTimestampDesc(Long chatId) {
            return findByChatIdOrderByTimestampAsc(chatId).stream()
                    .sorted((left, right) -> right.getTimestamp().compareTo(left.getTimestamp()))
                    .limit(10)
                    .toList();
        }

        @Override
        public void deleteByChatId(Long chatId) {
            messagesByChatId.remove(chatId);
        }

        @Override
        public void deleteByChatIdIn(List<Long> chatIds) {
            if (chatIds != null) {
                chatIds.forEach(messagesByChatId::remove);
            }
        }

        @Override
        public long countByChatId(Long chatId) {
            return messagesByChatId.getOrDefault(chatId, List.of()).size();
        }

        @Override
        public long count() {
            return messagesByChatId.values().stream().mapToLong(List::size).sum();
        }

        @Override
        public long countByUserId(Long userId) {
            return messagesByChatId.values().stream()
                    .flatMap(List::stream)
                    .filter(message -> message.getSenderId() != null && message.getSenderId().equals(userId))
                    .count();
        }

        @Override
        public boolean existsByChatId(Long chatId) {
            return messagesByChatId.containsKey(chatId) && !messagesByChatId.get(chatId).isEmpty();
        }

        @Override
        public Message save(Message message) {
            Long chatId = message.getChat() != null ? message.getChat().getId() : null;
            if (chatId != null) {
                messagesByChatId.computeIfAbsent(chatId, ignored -> new ArrayList<>()).add(message);
            }
            return message;
        }
    }
}
