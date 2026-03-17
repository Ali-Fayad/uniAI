package com.uniai.chat.infrastructure.persistence.adapter;

import com.uniai.chat.domain.model.Message;
import com.uniai.chat.domain.repository.MessageRepository;
import com.uniai.chat.infrastructure.persistence.repository.MessageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA-backed implementation of the domain {@link MessageRepository} interface.
 */
@Repository
@RequiredArgsConstructor
public class MessageRepositoryAdapter implements MessageRepository {

    private final MessageJpaRepository jpaRepository;

    @Override
    public List<Message> findByChatIdOrderByTimestampAsc(Long chatId) {
        return jpaRepository.findByChatIdOrderByTimestampAsc(chatId);
    }

    @Override
    public List<Message> findTop10ByChatIdOrderByTimestampDesc(Long chatId) {
        return jpaRepository.findTop10ByChatIdOrderByTimestampDesc(chatId);
    }

    @Override
    public void deleteByChatId(Long chatId) {
        jpaRepository.deleteByChatId(chatId);
    }

    @Override
    public void deleteByChatIdIn(List<Long> chatIds) {
        jpaRepository.deleteByChatIdIn(chatIds);
    }

    @Override
    public long countByChatId(Long chatId) {
        return jpaRepository.countByChatId(chatId);
    }

    @Override
    public boolean existsByChatId(Long chatId) {
        return jpaRepository.existsByChatId(chatId);
    }

    @Override
    public Message save(Message message) {
        return jpaRepository.save(message);
    }
}
