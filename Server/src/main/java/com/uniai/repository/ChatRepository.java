package com.uniai.repository;

import com.uniai.model.Chat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data. jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype. Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    List<Chat> findByUserUsernameOrderByUpdatedAtDesc(String username);

    // More efficient:  check if title exists (for first-message detection)
    @Query("SELECT c. title FROM Chat c WHERE c.id = :chatId")
    String findTitleById(@Param("chatId") Long chatId);
    
    // Alternative: fetch full chat if needed
    Optional<Chat> findById(Long chatId);
}