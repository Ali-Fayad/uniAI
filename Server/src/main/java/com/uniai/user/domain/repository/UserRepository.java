package com.uniai.user.domain.repository;

import com.uniai.user.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for the User aggregate.
 * Implementations live in the infrastructure layer.
 */
public interface UserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    User save(User user);

    void delete(User user);

    boolean deleteByEmail(String email);

    boolean deleteByUsername(String username);

    List<User> findAll();
}
