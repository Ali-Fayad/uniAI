package com.uniai.user.infrastructure.persistence.repository;

import com.uniai.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean deleteByEmail(String email);
    boolean deleteByUsername(String username);

    long countByRole(com.uniai.user.domain.valueobject.UserRole role);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) ORDER BY u.email ASC")
    List<User> searchByEmail(@Param("email") String email);
}
