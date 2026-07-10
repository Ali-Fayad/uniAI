package com.uniai.chat.infrastructure.persistence.adapter;

import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.infrastructure.persistence.repository.ChatJpaRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(ChatRepositoryAdapter.class);
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
        long startNanos = System.nanoTime();
        try {
            Chat saved = jpaRepository.save(chat);
            logger.debug("[PERSISTENCE] Chat saved id={} userId={} durationMs={}",
                    saved != null ? saved.getId() : null,
                    saved != null && saved.getUser() != null ? saved.getUser().getId() : null,
                    elapsedMillis(startNanos));
            return saved;
        } catch (RuntimeException ex) {
            logger.error("[PERSISTENCE] Chat save failed durationMs={} reason={}",
                    elapsedMillis(startNanos),
                    ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public void delete(Chat chat) {
        jpaRepository.delete(chat);
    }

    @Override
    public void deleteAll(List<Chat> chats) {
        jpaRepository.deleteAll(chats);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByUserId(Long userId) {
        return jpaRepository.countByUserId(userId);
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
