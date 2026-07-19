package com.uniai.user.domain.repository;

import com.uniai.user.domain.model.User;
import com.uniai.user.domain.valueobject.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for the User aggregate.
 * Implementations live in the infrastructure layer.
 */
public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    User save(User user);

    /** Flush-capable save used when a unique-key collision must be detected immediately. */
    default User saveAndFlush(User user) {
        return save(user);
    }

    void delete(User user);

    boolean deleteByEmail(String email);

    boolean deleteByUsername(String username);

    List<User> findAll();

    List<User> searchByEmail(String email);

    long count();

    long countByRole(UserRole role);
}
