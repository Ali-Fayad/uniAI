package com.uniai.chat.infrastructure.persistence.adapter;

import com.uniai.chat.application.memory.ConversationMemory;
import com.uniai.chat.application.port.out.ConversationMemoryPersistencePort;
import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.repository.ChatRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public class ConversationMemoryPersistenceAdapter implements ConversationMemoryPersistencePort {

    private static final Logger logger = LogManager.getLogger(ConversationMemoryPersistenceAdapter.class);

    private final ChatRepository chatRepository;

    public ConversationMemoryPersistenceAdapter(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationMemoryState load(Long chatId) {
        if (chatId == null) {
            return new ConversationMemoryState(ConversationMemory.empty(), 0L, null);
        }
        try {
            return chatRepository.findById(chatId)
                    .map(chat -> {
                        ConversationMemory memory = chat.getConversationMemory();
                        if (memory == null || !memory.hasValidSchema()) {
                            if (memory != null) {
                                logger.warn("[AI_MEMORY] Invalid memory schema loaded chatId={} schemaVersion={}",
                                        chatId,
                                        memory.schemaVersion());
                            }
                            return new ConversationMemoryState(ConversationMemory.empty(), chat.getMemoryVersion() == null ? 0L : chat.getMemoryVersion(), chat.getMemoryUpdatedAt());
                        }
                        return new ConversationMemoryState(
                                memory,
                                chat.getMemoryVersion() == null ? 0L : chat.getMemoryVersion(),
                                chat.getMemoryUpdatedAt()
                        );
                    })
                    .orElse(new ConversationMemoryState(ConversationMemory.empty(), 0L, null));
        } catch (RuntimeException ex) {
            logger.warn("[AI_MEMORY] Memory load failed chatId={} reason=unreadable-or-incompatible", chatId);
            return new ConversationMemoryState(ConversationMemory.empty(), 0L, null);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean save(Long chatId, long expectedMemoryVersion, ConversationMemory memory, LocalDateTime memoryUpdatedAt) {
        if (chatId == null || memory == null) {
            return false;
        }

        return chatRepository.findByIdForUpdate(chatId)
                .map(chat -> saveIfVersionMatches(chat, expectedMemoryVersion, memory, memoryUpdatedAt))
                .orElse(false);
    }

    private boolean saveIfVersionMatches(Chat chat, long expectedMemoryVersion, ConversationMemory memory, LocalDateTime memoryUpdatedAt) {
        long currentVersion = chat.getMemoryVersion() == null ? 0L : chat.getMemoryVersion();
        if (currentVersion != expectedMemoryVersion) {
            logger.debug("[AI_MEMORY] Memory version conflict chatId={} expectedVersion={} actualVersion={}",
                    chat.getId(),
                    expectedMemoryVersion,
                    currentVersion);
            return false;
        }

        chat.setConversationMemory(memory);
        chat.setMemoryUpdatedAt(memoryUpdatedAt == null ? LocalDateTime.now() : memoryUpdatedAt);
        chat.setMemoryVersion(currentVersion + 1L);
        chatRepository.save(chat);
        return true;
    }
}
