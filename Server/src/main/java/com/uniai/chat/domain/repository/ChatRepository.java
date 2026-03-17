package com.uniai.chat.domain.repository;

import com.uniai.chat.domain.model.Chat;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for the Chat aggregate.
 * Implementations live in the infrastructure layer.
 */
public interface ChatRepository {

    Optional<Chat> findById(Long id);

    List<Chat> findByUserUsernameOrderByUpdatedAtDesc(String username);

    String findTitleById(Long chatId);

    Chat save(Chat chat);

    void delete(Chat chat);

    void deleteAll(List<Chat> chats);
}
