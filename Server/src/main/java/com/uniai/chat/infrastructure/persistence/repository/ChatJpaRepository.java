package com.uniai.chat.infrastructure.persistence.repository;

import com.uniai.chat.domain.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatJpaRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByUserUsernameOrderByUpdatedAtDesc(String username);

    @Query("SELECT c.title FROM Chat c WHERE c.id = :chatId")
    String findTitleById(@Param("chatId") Long chatId);
}
