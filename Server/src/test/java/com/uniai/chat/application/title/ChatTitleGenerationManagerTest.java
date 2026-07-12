package com.uniai.chat.application.title;

import com.uniai.chat.application.budget.AiTokenEstimator;
import com.uniai.chat.application.budget.AiContextBudgetConfiguration;
import com.uniai.chat.application.dto.ai.AiRequest;
import com.uniai.chat.application.dto.ai.AiResponse;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.application.port.out.ChatTitlePromptPort;
import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatTitleGenerationManagerTest {

    private RecordingChatRepository chatRepository;
    private RecordingAiServicePort aiServicePort;
    private FixedChatTitlePromptPort promptPort;
    private ChatTitleGenerationManager manager;

    @BeforeEach
    void setUp() {
        chatRepository = new RecordingChatRepository();
        aiServicePort = new RecordingAiServicePort();
        promptPort = new FixedChatTitlePromptPort("Title prompt");
        AiTokenEstimator estimator = new AiTokenEstimator(new AiContextBudgetConfiguration(200, 24, 0, 0, 4, 16, java.util.Map.of()));
        ChatTitleGenerationConfiguration configuration = new ChatTitleGenerationConfiguration(true, 200, 24, 60);
        Executor directExecutor = Runnable::run;
        manager = new ChatTitleGenerationManager(chatRepository, aiServicePort, promptPort, estimator, configuration, directExecutor);
    }

    @Test
    void generateTitleIfNeededShouldUseOnlyFirstUserMessage() {
        chatRepository.save(chat(10L, null));

        manager.generateTitleIfNeeded(10L, "Compare AUB and LAU master's tuition");

        assertEquals(1, aiServicePort.callCount);
        assertEquals("Title prompt", aiServicePort.lastRequest.getSystemPrompt());
        assertEquals("Compare AUB and LAU master's tuition", aiServicePort.lastRequest.getUserMessage());
        assertTrue(aiServicePort.lastRequest.getConversationHistory().isEmpty());
        assertTrue(aiServicePort.lastRequest.getContext().isEmpty());
        assertNull(aiServicePort.lastRequest.getConversationMemory());
        assertEquals(24, aiServicePort.lastRequest.getMaxTokens());
        assertEquals("AUB vs LAU Tuition", chatRepository.findTitleById(10L));
    }

    @Test
    void generateTitleIfNeededShouldSkipExistingTitle() {
        chatRepository.save(chat(11L, "Existing title"));

        manager.generateTitleIfNeeded(11L, "Compare AUB and LAU master's tuition");

        assertEquals(0, aiServicePort.callCount);
        assertEquals("Existing title", chatRepository.findTitleById(11L));
    }

    @Test
    void generateTitleIfNeededShouldLeaveTitleNullWhenProviderFails() {
        chatRepository.save(chat(12L, null));
        aiServicePort.nextResponse = AiResponse.builder().fallback(true).content(null).provider("gemini").model("gemini").build();

        manager.generateTitleIfNeeded(12L, "Compare AUB and LAU master's tuition");

        assertEquals(1, aiServicePort.callCount);
        assertNull(chatRepository.findTitleById(12L));
    }

    @Test
    void generateTitleIfNeededShouldRejectInvalidOutput() {
        chatRepository.save(chat(13L, null));
        aiServicePort.nextResponse = AiResponse.builder().content("Title:\n**AUB vs LAU**").provider("gemini").model("gemini").build();

        manager.generateTitleIfNeeded(13L, "Compare AUB and LAU master's tuition");

        assertEquals(1, aiServicePort.callCount);
        assertNull(chatRepository.findTitleById(13L));
    }

    @Test
    void generateTitleIfNeededShouldRejectOverBudgetRequests() {
        ChatTitleGenerationConfiguration tinyConfiguration = new ChatTitleGenerationConfiguration(true, 1, 24, 60);
        manager = new ChatTitleGenerationManager(
                chatRepository,
                aiServicePort,
                promptPort,
                new AiTokenEstimator(new AiContextBudgetConfiguration(200, 24, 0, 0, 4, 16, java.util.Map.of())),
                tinyConfiguration,
                Runnable::run
        );
        chatRepository.save(chat(14L, null));

        manager.generateTitleIfNeeded(14L, "Compare AUB and LAU master's tuition");

        assertEquals(0, aiServicePort.callCount);
        assertNull(chatRepository.findTitleById(14L));
    }

    @Test
    void generateTitleIfNeededShouldRespectConcurrentSkip() {
        chatRepository.save(chat(15L, null));
        chatRepository.forceUpdateSkip = true;

        manager.generateTitleIfNeeded(15L, "Compare AUB and LAU master's tuition");

        assertEquals(1, aiServicePort.callCount);
        assertNull(chatRepository.findTitleById(15L));
    }

    @Test
    void generateTitleIfNeededShouldSkipPlaceholderProvider() {
        chatRepository.save(chat(16L, null));
        manager = new ChatTitleGenerationManager(
                chatRepository,
                aiServicePort,
                promptPort,
                new AiTokenEstimator(new AiContextBudgetConfiguration(200, 24, 0, 0, 4, 16, java.util.Map.of())),
                new ChatTitleGenerationConfiguration(true, 200, 24, 60),
                Runnable::run,
                "placeholder"
        );

        manager.generateTitleIfNeeded(16L, "Compare AUB and LAU master's tuition");

        assertEquals(0, aiServicePort.callCount);
        assertNull(chatRepository.findTitleById(16L));
    }

    @Test
    void generateTitleIfNeededShouldRejectSuspiciousProviderOutput() {
        chatRepository.save(chat(17L, null));
        aiServicePort.nextResponse = AiResponse.builder()
                .content("AI response to: Compare AUB and LAU tuition")
                .provider("gemini")
                .model("gemini")
                .build();

        manager.generateTitleIfNeeded(17L, "Compare AUB and LAU master's tuition");

        assertEquals(1, aiServicePort.callCount);
        assertNull(chatRepository.findTitleById(17L));
    }

    private Chat chat(Long id, String title) {
        return Chat.builder()
                .id(id)
                .user(User.builder().id(1L).username("alice").email("alice@example.com").password("pw").build())
                .title(title)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusMinutes(1))
                .build();
    }

    private static final class FixedChatTitlePromptPort implements ChatTitlePromptPort {
        private final String prompt;

        private FixedChatTitlePromptPort(String prompt) {
            this.prompt = prompt;
        }

        @Override
        public String getPrompt() {
            return prompt;
        }
    }

    private static final class RecordingAiServicePort implements AiServicePort {
        private int callCount;
        private AiRequest lastRequest;
        private AiResponse nextResponse = AiResponse.builder()
                .content("AUB vs LAU Tuition")
                .provider("gemini")
                .model("gemini-2.5-flash")
                .build();

        @Override
        public AiResponse generateResponse(AiRequest request) {
            callCount++;
            lastRequest = request;
            return nextResponse;
        }
    }

    private static final class RecordingChatRepository implements ChatRepository {
        private final LinkedHashMap<Long, Chat> storage = new LinkedHashMap<>();
        private boolean forceUpdateSkip;

        @Override
        public Optional<Chat> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<Chat> findByIdForUpdate(Long id) {
            return findById(id);
        }

        @Override
        public boolean updateTitleIfAbsent(Long chatId, String title) {
            if (forceUpdateSkip) {
                return false;
            }
            Chat chat = storage.get(chatId);
            if (chat == null || chat.getTitle() != null) {
                return false;
            }
            chat.setTitle(title);
            chat.setUpdatedAt(LocalDateTime.now());
            return true;
        }

        @Override
        public List<Chat> findByUserUsernameOrderByUpdatedAtDesc(String username) {
            return List.of();
        }

        @Override
        public String findTitleById(Long chatId) {
            Chat chat = storage.get(chatId);
            return chat != null ? chat.getTitle() : null;
        }

        @Override
        public Chat save(Chat chat) {
            storage.put(chat.getId(), chat);
            return chat;
        }

        @Override
        public void delete(Chat chat) {
            if (chat != null) {
                storage.remove(chat.getId());
            }
        }

        @Override
        public void deleteAll(List<Chat> chats) {
            if (chats != null) {
                chats.forEach(chat -> {
                    if (chat != null) {
                        storage.remove(chat.getId());
                    }
                });
            }
        }

        @Override
        public long count() {
            return storage.size();
        }

        @Override
        public long countByUserId(Long userId) {
            return storage.values().stream()
                    .filter(chat -> chat.getUser() != null && userId != null && userId.equals(chat.getUser().getId()))
                    .count();
        }
    }
}
