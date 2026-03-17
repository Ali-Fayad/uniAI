package com.uniai.chat.infrastructure.persistence.repository;

import com.uniai.chat.domain.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageJpaRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timestamp ASC")
    List<Message> findByChatIdOrderByTimestampAsc(@Param("chatId") Long chatId);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timestamp DESC LIMIT 10")
    List<Message> findTop10ByChatIdOrderByTimestampDesc(@Param("chatId") Long chatId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.chat.id = :chatId")
    void deleteByChatId(@Param("chatId") Long chatId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.chat.id IN :chatIds")
    void deleteByChatIdIn(@Param("chatIds") List<Long> chatIds);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId")
    long countByChatId(@Param("chatId") Long chatId);

    @Query("SELECT COUNT(m) > 0 FROM Message m WHERE m.chat.id = :chatId")
    boolean existsByChatId(@Param("chatId") Long chatId);
}
