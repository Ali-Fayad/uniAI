package com.uniai.chat.application.port.out;

import com.uniai.chat.application.memory.ConversationMemory;

import java.time.LocalDateTime;

public interface ConversationMemoryPersistencePort {

    ConversationMemoryState load(Long chatId);

    boolean save(Long chatId, long expectedMemoryVersion, ConversationMemory memory, LocalDateTime memoryUpdatedAt);

    record ConversationMemoryState(
            ConversationMemory memory,
            long memoryVersion,
            LocalDateTime memoryUpdatedAt
    ) {
    }
}
