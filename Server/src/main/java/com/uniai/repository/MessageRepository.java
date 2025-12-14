package com.uniai.repository;

import java.util.List;

import org.springframework.data.jpa. repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org. springframework.stereotype.Repository;

import com.uniai.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Load all messages (oldest first) for a chat
    List<Message> findByChatIdOrderByTimestampAsc(Long chatId);

    // Load last N messages (newest first)
    List<Message> findTop10ByChatIdOrderByTimestampDesc(Long chatId);
    
    // Delete all messages in a specific chat
    void deleteByChatId(Long chatId);
    
    // Delete all messages in multiple chats (for deleteAllChats)
    void deleteByChatIdIn(List<Long> chatIds);
    
    // Check if this is the first message (optional - for alternative implementation)
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatId = :chatId")
    long countByChatId(@Param("chatId") Long chatId);
    
    boolean existsByChatId(Long chatId);
}