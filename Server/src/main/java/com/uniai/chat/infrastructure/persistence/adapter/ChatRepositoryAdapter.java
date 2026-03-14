package com.uniai.chat.infrastructure.persistence.adapter;

import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.infrastructure.persistence.repository.ChatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-backed implementation of the domain {@link ChatRepository} interface.
 */
@Repository
@RequiredArgsConstructor
public class ChatRepositoryAdapter implements ChatRepository {

    private final ChatJpaRepository jpaRepository;

    @Override
    public Optional<Chat> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Chat> findByUserUsernameOrderByUpdatedAtDesc(String username) {
        return jpaRepository.findByUserUsernameOrderByUpdatedAtDesc(username);
    }

    @Override
    public String findTitleById(Long chatId) {
        return jpaRepository.findTitleById(chatId);
    }

    @Override
    public Chat save(Chat chat) {
        return jpaRepository.save(chat);
    }

    @Override
    public void delete(Chat chat) {
        jpaRepository.delete(chat);
    }

    @Override
    public void deleteAll(List<Chat> chats) {
        jpaRepository.deleteAll(chats);
    }
}
