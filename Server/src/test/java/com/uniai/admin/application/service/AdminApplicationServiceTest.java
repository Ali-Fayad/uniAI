package com.uniai.admin.application.service;

import com.uniai.admin.application.dto.response.AdminUserDetailsResponse;
import com.uniai.admin.application.dto.response.AdminUserFeedbackResponse;
import com.uniai.admin.application.dto.response.AdminUserSearchResponse;
import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.model.Message;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.domain.repository.MessageRepository;
import com.uniai.cvbuilder.application.dto.response.PersonalInfoResponse;
import com.uniai.cvbuilder.domain.model.CV;
import com.uniai.cvbuilder.domain.model.PersonalInfo;
import com.uniai.cvbuilder.domain.repository.CVRepository;
import com.uniai.cvbuilder.domain.repository.PersonalInfoRepository;
import com.uniai.feedback.domain.model.Feedback;
import com.uniai.feedback.domain.repository.FeedbackRepository;
import com.uniai.shared.exception.UserNotFoundException;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import com.uniai.user.domain.valueobject.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminApplicationServiceTest {

    @Test
    void searchUsersShouldReturnEmptyListForBlankEmail() {
        TestContext context = sampleContext();

        assertEquals(List.of(), context.service.searchUsersByEmail(null));
        assertEquals(List.of(), context.service.searchUsersByEmail(""));
        assertEquals(List.of(), context.service.searchUsersByEmail("   "));
    }

    @Test
    void searchUsersShouldMatchEmailCaseInsensitivelyAndSortAscending() {
        TestContext context = sampleContext();

        List<AdminUserSearchResponse> results = context.service.searchUsersByEmail("EXAMPLE");

        assertEquals(2, results.size());
        assertEquals("alice@example.com", results.get(0).getEmail());
        assertEquals("bob@example.com", results.get(1).getEmail());
        assertEquals("alice", results.get(0).getUsername());
        assertEquals("bob", results.get(1).getUsername());
    }

    @Test
    void getUserDetailsShouldReturnSelectedUserCounts() {
        TestContext context = sampleContext();

        AdminUserDetailsResponse response = context.service.getUserDetails(1L);

        assertEquals(1L, response.getId());
        assertEquals("alice", response.getUsername());
        assertEquals(2L, response.getChatCount());
        assertEquals(5L, response.getMessageCount());
        assertEquals(2.5, response.getAverageMessagesPerChat());
        assertEquals(3L, response.getCvCount());
        assertTrue(response.isVerified());
        assertFalse(response.isTwoFacAuth());
    }

    @Test
    void getUserDetailsShouldReturnZeroAverageWhenChatCountIsZero() {
        TestContext context = sampleContext();

        AdminUserDetailsResponse response = context.service.getUserDetails(2L);

        assertEquals(0L, response.getChatCount());
        assertEquals(0L, response.getMessageCount());
        assertEquals(0L, response.getCvCount());
        assertEquals(0.0, response.getAverageMessagesPerChat());
    }

    @Test
    void getUserPersonalInfoShouldReturnTargetUsersPersonalInfo() {
        TestContext context = sampleContext();

        PersonalInfoResponse response = context.service.getUserPersonalInfo(1L);

        assertEquals(1L, response.getUserId());
        assertTrue(response.isHasPersonalInfo());
        assertTrue(response.isFilled());
        assertEquals("123", response.getPhone());
        assertEquals("Beirut", response.getAddress());
        assertTrue(response.getEducation().isEmpty());
        assertTrue(response.getSkills().isEmpty());
    }

    @Test
    void getUserFeedbackShouldReturnNewestFirst() {
        TestContext context = sampleContext();

        List<AdminUserFeedbackResponse> responses = context.service.getUserFeedback(1L);

        assertEquals(2, responses.size());
        assertEquals(20L, responses.get(0).getId());
        assertEquals(10L, responses.get(1).getId());
        assertEquals("Second", responses.get(0).getContent());
    }

    @Test
    void getUserDetailsShouldThrowWhenUserDoesNotExist() {
        TestContext context = sampleContext();

        assertThrows(UserNotFoundException.class, () -> context.service.getUserDetails(999L));
    }

    @Test
    void deleteUserShouldDeleteUserAndFeedbackForRegularUser() {
        TestContext context = sampleContext();

        context.service.deleteUser("alice@example.com", 2L);

        assertEquals(List.of(2L), context.feedbackRepository.deletedUserIds);
        assertEquals(List.of("feedback:2", "user:2"), context.operations);
        assertThrows(UserNotFoundException.class, () -> context.service.getUserDetails(2L));
    }

    @Test
    void deleteAdminShouldSucceedWhenAnotherAdminExists() {
        TestContext context = sampleContext();
        context.users.put(3L, user(3L, "carol@example.com", "carol", "Carol", "Clark", UserRole.ADMIN));
        context.feedback.add(feedback(30L, 3L, "Admin feedback", LocalDateTime.parse("2026-01-03T10:00:00")));

        context.service.deleteUser("alice@example.com", 3L);

        assertEquals(1L, context.userRepository.countByRoleCalls.get());
        assertThrows(UserNotFoundException.class, () -> context.service.getUserDetails(3L));
    }

    @Test
    void deleteSelfShouldBeForbidden() {
        TestContext context = sampleContext();

        assertThrows(com.uniai.shared.exception.SelfDeleteNotAllowedException.class,
                () -> context.service.deleteUser("alice@example.com", 1L));
        assertEquals(List.of(), context.operations);
    }

    @Test
    void deleteLastAdminShouldBeBlocked() {
        TestContext context = sampleContext();

        assertThrows(com.uniai.shared.exception.LastAdminProtectionException.class,
                () -> context.service.deleteUser("bob@example.com", 1L));
        assertEquals(1L, context.userRepository.countByRoleCalls.get());
        assertEquals(List.of(), context.operations);
    }

    @Test
    void deleteMissingUserShouldThrow() {
        TestContext context = sampleContext();

        assertThrows(UserNotFoundException.class, () -> context.service.deleteUser("alice@example.com", 999L));
    }

    @Test
    void deleteUserShouldInvokeFeedbackCleanupBeforeUserDeletion() {
        TestContext context = sampleContext();
        context.users.put(4L, user(4L, "dave@example.com", "dave", "Dave", "Doe", UserRole.USER));
        context.feedback.add(feedback(40L, 4L, "Cleanup", LocalDateTime.parse("2026-01-04T10:00:00")));

        context.service.deleteUser("alice@example.com", 4L);

        assertEquals(List.of("feedback:4", "user:4"), context.operations);
        assertEquals(List.of(4L), context.feedbackRepository.deletedUserIds);
    }

    @Test
    void updateUserRoleShouldPromoteUserToAdmin() {
        TestContext context = sampleContext();

        AdminUserDetailsResponse response = context.service.updateUserRole("alice@example.com", 2L, UserRole.ADMIN);

        assertEquals(UserRole.ADMIN.name(), response.getRole());
        assertEquals(UserRole.ADMIN, context.users.get(2L).getRole());
    }

    @Test
    void updateUserRoleShouldDemoteAdminWhenAnotherAdminExists() {
        TestContext context = sampleContext();
        context.users.put(4L, user(4L, "carol@example.com", "carol", "Carol", "Clark", UserRole.ADMIN));

        AdminUserDetailsResponse response = context.service.updateUserRole("alice@example.com", 4L, UserRole.USER);

        assertEquals(UserRole.USER.name(), response.getRole());
        assertEquals(UserRole.USER, context.users.get(4L).getRole());
        assertEquals(1, context.userRepository.countByRoleCalls.get());
    }

    @Test
    void updateUserRoleShouldBeIdempotentForDuplicateRole() {
        TestContext context = sampleContext();

        AdminUserDetailsResponse response = context.service.updateUserRole("alice@example.com", 2L, UserRole.USER);

        assertEquals(UserRole.USER.name(), response.getRole());
        assertEquals(0, context.userRepository.countByRoleCalls.get());
    }

    @Test
    void updateUserRoleShouldBlockSelfDemotion() {
        TestContext context = sampleContext();

        assertThrows(com.uniai.shared.exception.SelfDemotionNotAllowedException.class,
                () -> context.service.updateUserRole("alice@example.com", 1L, UserRole.USER));
    }

    @Test
    void updateUserRoleShouldBlockLastAdminDemotion() {
        TestContext context = sampleContext();

        assertThrows(com.uniai.shared.exception.LastAdminProtectionException.class,
                () -> context.service.updateUserRole("bob@example.com", 1L, UserRole.USER));
        assertEquals(1, context.userRepository.countByRoleCalls.get());
    }

    private static TestContext sampleContext() {
        TestContext context = new TestContext();

        User alice = user(1L, "alice@example.com", "alice", "Alice", "Anderson", UserRole.ADMIN);
        User bob = user(2L, "bob@example.com", "bob", "Bob", "Brown", UserRole.USER);
        User zoe = user(3L, "zoe@sample.com", "zoe", "Zoe", "Zimmer", UserRole.USER);
        context.users.put(alice.getId(), alice);
        context.users.put(bob.getId(), bob);
        context.users.put(zoe.getId(), zoe);

        Chat aliceChat1 = chat(101L, alice);
        Chat aliceChat2 = chat(102L, alice);
        context.chats.add(aliceChat1);
        context.chats.add(aliceChat2);

        context.messages.add(message(1001L, aliceChat1, 1L));
        context.messages.add(message(1002L, aliceChat1, 1L));
        context.messages.add(message(1003L, aliceChat1, 1L));
        context.messages.add(message(1004L, aliceChat2, 1L));
        context.messages.add(message(1005L, aliceChat2, 1L));

        context.cvs.add(cv(201L, 1L));
        context.cvs.add(cv(202L, 1L));
        context.cvs.add(cv(203L, 1L));

        context.feedback.add(feedback(10L, 1L, "First", LocalDateTime.parse("2026-01-01T10:00:00")));
        context.feedback.add(feedback(20L, 1L, "Second", LocalDateTime.parse("2026-01-02T10:00:00")));

        context.personalInfos.put(1L, PersonalInfo.builder()
                .userId(1L)
                .phone("123")
                .address("Beirut")
                .summary("Summary")
                .isFilled(true)
                .educationJson("[]")
                .skillsJson("[]")
                .languagesJson("[]")
                .experienceJson("[]")
                .projectsJson("[]")
                .certificatesJson("[]")
                .build());

        return context;
    }

    private static User user(Long id, String email, String username, String firstName, String lastName, UserRole role) {
        return User.builder()
                .id(id)
                .email(email)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .password("password")
                .role(role)
                .isVerified(true)
                .isTwoFacAuth(false)
                .build();
    }

    private static Chat chat(Long id, User user) {
        return Chat.builder()
                .id(id)
                .user(user)
                .title("Chat " + id)
                .build();
    }

    private static Message message(Long id, Chat chat, Long senderId) {
        return Message.builder()
                .id(id)
                .chat(chat)
                .senderId(senderId)
                .content("Message " + id)
                .timestamp(LocalDateTime.parse("2026-01-01T10:00:00"))
                .build();
    }

    private static CV cv(Long id, Long userId) {
        return CV.builder()
                .id(id)
                .userId(userId)
                .cvName("CV " + id)
                .build();
    }

    private static Feedback feedback(Long id, Long userId, String content, LocalDateTime createdAt) {
        return Feedback.builder()
                .id(id)
                .userId(userId)
                .content(content)
                .rating(5)
                .createdAt(createdAt)
                .build();
    }

    private static final class TestContext {
        private final Map<Long, User> users = new HashMap<>();
        private final List<Chat> chats = new ArrayList<>();
        private final List<Message> messages = new ArrayList<>();
        private final List<Feedback> feedback = new ArrayList<>();
        private final List<CV> cvs = new ArrayList<>();
        private final Map<Long, PersonalInfo> personalInfos = new HashMap<>();
        private final List<String> operations = new ArrayList<>();

        private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
        private final InMemoryChatRepository chatRepository = new InMemoryChatRepository();
        private final InMemoryMessageRepository messageRepository = new InMemoryMessageRepository();
        private final InMemoryFeedbackRepository feedbackRepository = new InMemoryFeedbackRepository();
        private final InMemoryCVRepository cvRepository = new InMemoryCVRepository();
        private final InMemoryPersonalInfoRepository personalInfoRepository = new InMemoryPersonalInfoRepository();

        private final AdminApplicationService service = new AdminApplicationService(
                userRepository,
                chatRepository,
                messageRepository,
                feedbackRepository,
                cvRepository,
                personalInfoRepository
        );

        private final class InMemoryUserRepository implements UserRepository {
            private final AtomicInteger countByRoleCalls = new AtomicInteger();

            @Override
            public Optional<User> findById(Long id) {
                return Optional.ofNullable(users.get(id));
            }

            @Override
            public Optional<User> findByEmail(String email) {
                if (email == null) {
                    return Optional.empty();
                }
                return users.values().stream()
                        .filter(user -> user.getEmail() != null && user.getEmail().equalsIgnoreCase(email))
                        .findFirst();
            }

            @Override
            public Optional<User> findByUsername(String username) {
                if (username == null) {
                    return Optional.empty();
                }
                return users.values().stream()
                        .filter(user -> user.getUsername() != null && user.getUsername().equalsIgnoreCase(username))
                        .findFirst();
            }

            @Override
            public boolean existsByEmail(String email) {
                return findByEmail(email).isPresent();
            }

            @Override
            public boolean existsByUsername(String username) {
                return findByUsername(username).isPresent();
            }

            @Override
            public User save(User user) {
                users.put(user.getId(), user);
                return user;
            }

            @Override
            public void delete(User user) {
                if (user != null) {
                    operations.add("user:" + user.getId());
                    users.remove(user.getId());
                }
            }

            @Override
            public boolean deleteByEmail(String email) {
                return findByEmail(email).map(user -> {
                    users.remove(user.getId());
                    return true;
                }).orElse(false);
            }

            @Override
            public boolean deleteByUsername(String username) {
                return findByUsername(username).map(user -> {
                    users.remove(user.getId());
                    return true;
                }).orElse(false);
            }

            @Override
            public List<User> findAll() {
                return new ArrayList<>(users.values());
            }

            @Override
            public List<User> searchByEmail(String email) {
                if (email == null || email.isBlank()) {
                    return List.of();
                }
                String needle = email.toLowerCase();
                return users.values().stream()
                        .filter(user -> user.getEmail() != null && user.getEmail().toLowerCase().contains(needle))
                        .sorted(Comparator.comparing(User::getEmail, String.CASE_INSENSITIVE_ORDER))
                        .toList();
            }

            @Override
            public long count() {
                return users.size();
            }

            @Override
            public long countByRole(UserRole role) {
                countByRoleCalls.incrementAndGet();
                return users.values().stream()
                        .filter(user -> user.getRole() == role)
                        .count();
            }
        }

        private final class InMemoryChatRepository implements ChatRepository {
            @Override
            public Optional<Chat> findById(Long id) {
                return chats.stream().filter(chat -> chat.getId().equals(id)).findFirst();
            }

            @Override
            public List<Chat> findByUserUsernameOrderByUpdatedAtDesc(String username) {
                return chats.stream()
                        .filter(chat -> chat.getUser() != null
                                && chat.getUser().getUsername() != null
                                && chat.getUser().getUsername().equalsIgnoreCase(username))
                        .sorted(Comparator.comparing(Chat::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                        .toList();
            }

            @Override
            public String findTitleById(Long chatId) {
                return findById(chatId).map(Chat::getTitle).orElse(null);
            }

            @Override
            public Chat save(Chat chat) {
                chats.add(chat);
                return chat;
            }

            @Override
            public void delete(Chat chat) {
                if (chat != null) {
                    chats.removeIf(item -> item.getId().equals(chat.getId()));
                }
            }

            @Override
            public void deleteAll(List<Chat> items) {
                chats.removeAll(items);
            }

            @Override
            public long count() {
                return chats.size();
            }

            @Override
            public long countByUserId(Long userId) {
                return chats.stream()
                        .filter(chat -> chat.getUser() != null && userId.equals(chat.getUser().getId()))
                        .count();
            }
        }

        private final class InMemoryMessageRepository implements MessageRepository {
            @Override
            public List<Message> findByChatIdOrderByTimestampAsc(Long chatId) {
                return messages.stream()
                        .filter(message -> chatId.equals(message.getChatId()))
                        .sorted(Comparator.comparing(Message::getTimestamp))
                        .toList();
            }

            @Override
            public List<Message> findTop10ByChatIdOrderByTimestampDesc(Long chatId) {
                return messages.stream()
                        .filter(message -> chatId.equals(message.getChatId()))
                        .sorted(Comparator.comparing(Message::getTimestamp).reversed())
                        .limit(10)
                        .toList();
            }

            @Override
            public void deleteByChatId(Long chatId) {
                messages.removeIf(message -> chatId.equals(message.getChatId()));
            }

            @Override
            public void deleteByChatIdIn(List<Long> chatIds) {
                messages.removeIf(message -> chatIds.contains(message.getChatId()));
            }

            @Override
            public long countByChatId(Long chatId) {
                return messages.stream().filter(message -> chatId.equals(message.getChatId())).count();
            }

            @Override
            public long count() {
                return messages.size();
            }

            @Override
            public long countByUserId(Long userId) {
                return messages.stream()
                        .filter(message -> message.getChat() != null
                                && message.getChat().getUser() != null
                                && userId.equals(message.getChat().getUser().getId()))
                        .count();
            }

            @Override
            public boolean existsByChatId(Long chatId) {
                return countByChatId(chatId) > 0;
            }

            @Override
            public Message save(Message message) {
                messages.add(message);
                return message;
            }
        }

        private final class InMemoryFeedbackRepository implements FeedbackRepository {
            @Override
            public Feedback save(Feedback item) {
                feedback.add(item);
                return item;
            }

            @Override
            public long count() {
                return feedback.size();
            }

            @Override
            public List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId) {
                return feedback.stream()
                        .filter(item -> userId.equals(item.getUserId()))
                        .sorted(Comparator.comparing(Feedback::getCreatedAt).reversed())
                        .toList();
            }

            @Override
            public void deleteByUserId(Long userId) {
                operations.add("feedback:" + userId);
                deletedUserIds.add(userId);
                feedback.removeIf(item -> userId.equals(item.getUserId()));
            }

            private final List<Long> deletedUserIds = new ArrayList<>();
        }

        private final class InMemoryCVRepository implements CVRepository {
            @Override
            public Optional<CV> findById(Long id) {
                return cvs.stream().filter(cv -> cv.getId().equals(id)).findFirst();
            }

            @Override
            public List<CV> findByUserId(Long userId) {
                return cvs.stream().filter(cv -> userId.equals(cv.getUserId())).toList();
            }

            @Override
            public Optional<CV> findDefaultByUserId(Long userId) {
                return cvs.stream()
                        .filter(cv -> userId.equals(cv.getUserId()) && cv.isDefault())
                        .findFirst();
            }

            @Override
            public long countByUserId(Long userId) {
                return cvs.stream().filter(cv -> userId.equals(cv.getUserId())).count();
            }

            @Override
            public CV save(CV cv) {
                cvs.add(cv);
                return cv;
            }

            @Override
            public void delete(CV cv) {
                if (cv != null) {
                    cvs.removeIf(item -> item.getId().equals(cv.getId()));
                }
            }

            @Override
            public void deleteById(Long id) {
                cvs.removeIf(item -> item.getId().equals(id));
            }
        }

        private final class InMemoryPersonalInfoRepository implements PersonalInfoRepository {
            @Override
            public Optional<PersonalInfo> findByUserId(Long userId) {
                return Optional.ofNullable(personalInfos.get(userId));
            }

            @Override
            public PersonalInfo save(PersonalInfo personalInfo) {
                personalInfos.put(personalInfo.getUserId(), personalInfo);
                return personalInfo;
            }
        }
    }
}
