package com.uniai.user.application.service;

import com.uniai.feedback.domain.model.Feedback;
import com.uniai.feedback.domain.repository.FeedbackRepository;
import com.uniai.user.application.dto.command.DeleteUserCommand;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import com.uniai.user.domain.valueobject.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserApplicationServiceTest {

    @Test
    void deleteUserShouldCleanupFeedbackBeforeDeletingUser() {
        TestContext context = new TestContext();
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .username("user")
                .password("encoded-password")
                .role(UserRole.USER)
                .isVerified(true)
                .isTwoFacAuth(false)
                .build();
        context.users.add(user);

        context.feedback.add(feedback(10L, 1L, "First"));
        context.feedback.add(feedback(11L, 1L, "Second"));

        context.service.deleteUser("user@example.com", new DeleteUserCommand("raw-password"));

        assertEquals(List.of("feedback:1", "user:1"), context.operations);
        assertEquals(List.of(1L), context.feedbackRepository.deletedUserIds);
    }

    @Test
    void deleteUserShouldKeepPasswordValidationBehavior() {
        TestContext context = new TestContext();
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .username("user")
                .password("encoded-password")
                .role(UserRole.USER)
                .isVerified(true)
                .isTwoFacAuth(false)
                .build();
        context.users.add(user);
        context.passwordEncoder.matchesResult = false;

        assertThrows(com.uniai.shared.exception.InvalidEmailOrPassword.class,
                () -> context.service.deleteUser("user@example.com", new DeleteUserCommand("wrong-password")));

        assertEquals(List.of(), context.operations);
        assertEquals(List.of(), context.feedbackRepository.deletedUserIds);
    }

    private static Feedback feedback(Long id, Long userId, String content) {
        return Feedback.builder()
                .id(id)
                .userId(userId)
                .content(content)
                .rating(5)
                .createdAt(java.time.LocalDateTime.parse("2026-01-01T10:00:00"))
                .build();
    }

    private static final class TestContext {
        private final List<User> users = new ArrayList<>();
        private final List<Feedback> feedback = new ArrayList<>();
        private final List<String> operations = new ArrayList<>();
        private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
        private final InMemoryFeedbackRepository feedbackRepository = new InMemoryFeedbackRepository();
        private final StubPasswordEncoder passwordEncoder = new StubPasswordEncoder();
        private final UserApplicationService service = new UserApplicationService(userRepository, feedbackRepository, passwordEncoder);

        private final class InMemoryUserRepository implements UserRepository {
            @Override public Optional<User> findById(Long id) { return users.stream().filter(user -> id.equals(user.getId())).findFirst(); }
            @Override public Optional<User> findByEmail(String email) { return users.stream().filter(user -> user.getEmail().equalsIgnoreCase(email)).findFirst(); }
            @Override public Optional<User> findByUsername(String username) { return Optional.empty(); }
            @Override public boolean existsByEmail(String email) { return findByEmail(email).isPresent(); }
            @Override public boolean existsByUsername(String username) { return false; }
            @Override public User save(User user) { return user; }
            @Override public void delete(User user) { operations.add("user:" + user.getId()); users.removeIf(item -> item.getId().equals(user.getId())); }
            @Override public boolean deleteByEmail(String email) { return false; }
            @Override public boolean deleteByUsername(String username) { return false; }
            @Override public List<User> findAll() { return List.copyOf(users); }
            @Override public List<User> searchByEmail(String email) { return List.of(); }
            @Override public long count() { return users.size(); }
            @Override public long countByRole(UserRole role) { return 0L; }
        }

        private final class InMemoryFeedbackRepository implements FeedbackRepository {
            @Override public Feedback save(Feedback feedbackItem) { feedback.add(feedbackItem); return feedbackItem; }
            @Override public long count() { return feedback.size(); }
            @Override public List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId) { return List.of(); }
            @Override public void deleteByUserId(Long userId) {
                operations.add("feedback:" + userId);
                deletedUserIds.add(userId);
                feedback.removeIf(item -> userId.equals(item.getUserId()));
            }
            private final List<Long> deletedUserIds = new ArrayList<>();
        }

        private final class StubPasswordEncoder implements PasswordEncoder {
            private boolean matchesResult = true;

            @Override public String encode(CharSequence rawPassword) { return "encoded-password"; }
            @Override public boolean matches(CharSequence rawPassword, String encodedPassword) { return matchesResult; }
        }
    }
}
