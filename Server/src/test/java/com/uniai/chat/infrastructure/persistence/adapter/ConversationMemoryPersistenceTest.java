package com.uniai.chat.infrastructure.persistence.adapter;

import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.memory.ConversationPreferences;
import com.uniai.chat.application.memory.MemoryUniversityRef;
import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.application.port.out.ConversationMemoryPersistencePort.ConversationMemoryState;
import com.uniai.user.domain.model.User;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversationMemoryPersistenceTest {

    @Test
    void loadShouldReturnEmptyMemoryWhenNullAndSaveShouldIncrementVersion() {
        InMemoryChatRepository chatRepository = new InMemoryChatRepository();
        ConversationMemoryPersistenceAdapter adapter = new ConversationMemoryPersistenceAdapter(chatRepository);

        Chat chat = chat(1L, null, 0L);
        chatRepository.save(chat);

        ConversationMemoryState loaded = adapter.load(1L);
        assertTrue(loaded.memory().isEmpty());
        assertEquals(0L, loaded.memoryVersion());

        ConversationMemory memory = new ConversationMemory(
                ConversationMemory.SCHEMA_VERSION,
                List.of(new MemoryUniversityRef(1L, "American University of Beirut", "AUB")),
                List.of("MASTER"),
                "PROGRAM_LOOKUP",
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new ConversationPreferences("ENGLISH", null, null)
        );

        assertTrue(adapter.save(1L, 0L, memory, LocalDateTime.now()));
        ConversationMemoryState saved = adapter.load(1L);
        assertEquals(memory, saved.memory());
        assertEquals(1L, saved.memoryVersion());
        assertNotNull(saved.memoryUpdatedAt());
    }

    @Test
    void saveShouldRejectVersionConflict() {
        InMemoryChatRepository chatRepository = new InMemoryChatRepository();
        ConversationMemoryPersistenceAdapter adapter = new ConversationMemoryPersistenceAdapter(chatRepository);

        Chat chat = chat(2L, ConversationMemory.empty(), 2L);
        chatRepository.save(chat);

        boolean saved = adapter.save(2L, 1L, ConversationMemory.empty(), LocalDateTime.now());

        assertFalse(saved);
        assertEquals(2L, chatRepository.storage.get(2L).getMemoryVersion());
    }

    @Test
    void loadShouldFallbackToEmptyMemoryAndAvoidLoggingRawPayloadOnFailure() {
        ChatRepository chatRepository = new ChatRepository() {
            @Override
            public Optional<Chat> findById(Long id) {
                throw new RuntimeException("malformed {\"conversation_memory\":true}");
            }

            @Override
            public Optional<Chat> findByIdForUpdate(Long id) {
                return findById(id);
            }

            @Override
            public List<Chat> findByUserUsernameOrderByUpdatedAtDesc(String username) {
                return List.of();
            }

            @Override
            public String findTitleById(Long chatId) {
                return null;
            }

            @Override
            public Chat save(Chat chat) {
                return chat;
            }

            @Override
            public void delete(Chat chat) {
            }

            @Override
            public void deleteAll(List<Chat> chats) {
            }

            @Override
            public long count() {
                return 0L;
            }

            @Override
            public long countByUserId(Long userId) {
                return 0L;
            }
        };
        ConversationMemoryPersistenceAdapter adapter = new ConversationMemoryPersistenceAdapter(chatRepository);

        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(ConversationMemoryPersistenceAdapter.class);
        logger.addAppender(appender);
        try {
            ConversationMemoryState loaded = adapter.load(99L);

            assertTrue(loaded.memory().isEmpty());
            assertEquals(0L, loaded.memoryVersion());
            assertTrue(appender.list.stream().anyMatch(event -> event.getLevel().levelStr.equals("WARN")));
            assertTrue(appender.list.stream().noneMatch(event -> event.getFormattedMessage().contains("malformed")));
            assertTrue(appender.list.stream().noneMatch(event -> event.getFormattedMessage().contains("{")));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private Chat chat(Long id, ConversationMemory memory, Long version) {
        return Chat.builder()
                .id(id)
                .user(User.builder().id(99L).username("alice").email("alice@example.com").password("pw").build())
                .title("chat")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .conversationMemory(memory)
                .memoryVersion(version)
                .build();
    }

    private static final class InMemoryChatRepository implements ChatRepository {
        private final Map<Long, Chat> storage = new HashMap<>();

        @Override
        public Optional<Chat> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<Chat> findByIdForUpdate(Long id) {
            return findById(id);
        }

        @Override
        public List<Chat> findByUserUsernameOrderByUpdatedAtDesc(String username) {
            return storage.values().stream().toList();
        }

        @Override
        public String findTitleById(Long chatId) {
            return storage.containsKey(chatId) ? storage.get(chatId).getTitle() : null;
        }

        @Override
        public Chat save(Chat chat) {
            storage.put(chat.getId(), chat);
            return chat;
        }

        @Override
        public void delete(Chat chat) {
            storage.remove(chat.getId());
        }

        @Override
        public void deleteAll(List<Chat> chats) {
            chats.forEach(chat -> storage.remove(chat.getId()));
        }

        @Override
        public long count() {
            return storage.size();
        }

        @Override
        public long countByUserId(Long userId) {
            return storage.values().stream().filter(chat -> chat.getUser() != null && userId != null && userId.equals(chat.getUser().getId())).count();
        }
    }

}
