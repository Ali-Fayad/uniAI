package com.uniai.chat.infrastructure.persistence.adapter;

import com.uniai.chat.domain.model.Message;
import com.uniai.chat.domain.repository.MessageRepository;
import com.uniai.chat.infrastructure.persistence.repository.MessageJpaRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA-backed implementation of the domain {@link MessageRepository} interface.
 */
@Repository
@RequiredArgsConstructor
public class MessageRepositoryAdapter implements MessageRepository {

    private static final Logger logger = LogManager.getLogger(MessageRepositoryAdapter.class);
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
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByUserId(Long userId) {
        return jpaRepository.countByUserId(userId);
    }

    @Override
    public boolean existsByChatId(Long chatId) {
        return jpaRepository.existsByChatId(chatId);
    }

    @Override
    public Message save(Message message) {
        long startNanos = System.nanoTime();
        try {
            Message saved = jpaRepository.save(message);
            logger.debug("[PERSISTENCE] Message saved id={} chatId={} senderId={} contentLength={} durationMs={}",
                    saved != null ? saved.getId() : null,
                    saved != null ? saved.getChatId() : null,
                    saved != null ? saved.getSenderId() : null,
                    saved != null && saved.getContent() != null ? saved.getContent().length() : 0,
                    elapsedMillis(startNanos));
            return saved;
        } catch (RuntimeException ex) {
            logger.error("[PERSISTENCE] Message save failed durationMs={} reason={}",
                    elapsedMillis(startNanos),
                    ex.getMessage(), ex);
            throw ex;
        }
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
