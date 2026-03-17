package com.uniai.user.infrastructure.persistence.adapter;

import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import com.uniai.user.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-backed implementation of the domain {@link UserRepository} interface.
 * Lives in the infrastructure layer — the application layer stays decoupled from Spring Data.
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public void delete(User user) {
        jpaRepository.delete(user);
    }

    @Override
    public boolean deleteByEmail(String email) {
        return jpaRepository.deleteByEmail(email);
    }

    @Override
    public boolean deleteByUsername(String username) {
        return jpaRepository.deleteByUsername(username);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll();
    }
}
