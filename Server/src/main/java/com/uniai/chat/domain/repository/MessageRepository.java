package com.uniai.chat.domain.repository;

import com.uniai.chat.domain.model.Message;

import java.util.List;

/**
 * Domain repository interface for Message.
 * Implementations live in the infrastructure layer.
 */
public interface MessageRepository {

    List<Message> findByChatIdOrderByTimestampAsc(Long chatId);

    List<Message> findTop10ByChatIdOrderByTimestampDesc(Long chatId);

    void deleteByChatId(Long chatId);

    void deleteByChatIdIn(List<Long> chatIds);

    long countByChatId(Long chatId);

    boolean existsByChatId(Long chatId);

    Message save(Message message);
}
