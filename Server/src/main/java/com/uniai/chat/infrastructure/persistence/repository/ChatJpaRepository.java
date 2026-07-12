package com.uniai.chat.infrastructure.persistence.repository;

import com.uniai.chat.domain.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface ChatJpaRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByUserUsernameOrderByUpdatedAtDesc(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Chat c WHERE c.id = :id")
    Optional<Chat> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT COUNT(c) FROM Chat c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT c.title FROM Chat c WHERE c.id = :chatId")
    String findTitleById(@Param("chatId") Long chatId);
}
