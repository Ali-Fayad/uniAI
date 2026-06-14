package com.uniai.admin.presentation.controller;

import com.uniai.admin.application.dto.response.AdminOverviewResponse;
import com.uniai.admin.application.dto.response.AdminUserDetailsResponse;
import com.uniai.admin.application.dto.response.AdminUserFeedbackResponse;
import com.uniai.admin.application.dto.response.AdminUserSearchResponse;
import com.uniai.admin.application.service.AdminApplicationService;
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
import com.uniai.shared.infrastructure.jwt.JwtFacade;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AdminControllerTest {

    @Test
    void healthShouldReturnAdminAccessGrantedMessage() {
        StubAdminApplicationService service = new StubAdminApplicationService();
        AdminController controller = new AdminController(new StubJwtFacade("alice@example.com"), service);

        AdminController.AdminHealthResponse response = controller.health().getBody();

        assertEquals("Admin access granted", response.message());
    }

    @Test
    void overviewShouldDelegateToApplicationService() {
        AdminOverviewResponse expected = AdminOverviewResponse.builder()
                .totalUsers(1)
                .totalChats(2)
                .totalMessages(3)
                .totalFeedback(4)
                .averageChatsPerUser(2.0)
                .averageMessagesPerChat(1.5)
                .averageMessagesPerUser(3.0)
                .build();

        StubAdminApplicationService service = new StubAdminApplicationService();
        service.overview = expected;

        AdminController controller = new AdminController(new StubJwtFacade("alice@example.com"), service);

        assertSame(expected, controller.overview().getBody());
    }

    @Test
    void searchUsersShouldDelegateToApplicationService() {
        List<AdminUserSearchResponse> expected = List.of(
                AdminUserSearchResponse.builder()
                        .id(1L)
                        .email("alice@example.com")
                        .username("alice")
                        .firstName("Alice")
                        .lastName("Anderson")
                        .role("ADMIN")
                        .build()
        );

        StubAdminApplicationService service = new StubAdminApplicationService();
        service.searchResults = expected;

        AdminController controller = new AdminController(new StubJwtFacade("alice@example.com"), service);

        assertSame(expected, controller.searchUsers("alice").getBody());
        assertEquals("alice", service.lastEmail);
    }

    @Test
    void getUserDetailsShouldDelegateToApplicationService() {
        AdminUserDetailsResponse expected = AdminUserDetailsResponse.builder()
                .id(7L)
                .username("zoe")
                .firstName("Zoe")
                .lastName("Zimmer")
                .email("zoe@example.com")
                .role("USER")
                .isVerified(true)
                .isTwoFacAuth(false)
                .chatCount(1L)
                .messageCount(2L)
                .averageMessagesPerChat(2.0)
                .cvCount(1L)
                .build();

        StubAdminApplicationService service = new StubAdminApplicationService();
        service.details = expected;

        AdminController controller = new AdminController(new StubJwtFacade("alice@example.com"), service);

        assertSame(expected, controller.getUserDetails(7L).getBody());
        assertEquals(7L, service.lastUserId);
    }

    @Test
    void getUserPersonalInfoShouldDelegateToApplicationService() {
        PersonalInfoResponse expected = PersonalInfoResponse.builder()
                .userId(7L)
                .hasPersonalInfo(true)
                .isFilled(true)
                .education(List.of())
                .skills(List.of())
                .languages(List.of())
                .experience(List.of())
                .projects(List.of())
                .certificates(List.of())
                .build();

        StubAdminApplicationService service = new StubAdminApplicationService();
        service.personalInfo = expected;

        AdminController controller = new AdminController(new StubJwtFacade("alice@example.com"), service);

        assertSame(expected, controller.getUserPersonalInfo(7L).getBody());
        assertEquals(7L, service.lastUserId);
    }

    @Test
    void getUserFeedbackShouldDelegateToApplicationService() {
        List<AdminUserFeedbackResponse> expected = List.of(
                AdminUserFeedbackResponse.builder()
                        .id(1L)
                        .rating(5)
                        .content("Great")
                        .createdAt(java.time.LocalDateTime.parse("2026-01-01T10:00:00"))
                        .build()
        );

        StubAdminApplicationService service = new StubAdminApplicationService();
        service.feedback = expected;

        AdminController controller = new AdminController(new StubJwtFacade("alice@example.com"), service);

        assertSame(expected, controller.getUserFeedback(7L).getBody());
        assertEquals(7L, service.lastUserId);
    }

    @Test
    void deleteUserShouldDelegateToApplicationServiceAndReturnNoContent() {
        StubAdminApplicationService service = new StubAdminApplicationService();
        AdminController controller = new AdminController(new StubJwtFacade("admin@example.com"), service);

        assertEquals(204, controller.deleteUser(77L).getStatusCode().value());
        assertEquals("admin@example.com", service.deletedEmail);
        assertEquals(77L, service.deletedUserId);
    }

    private static final class StubAdminApplicationService extends AdminApplicationService {
        private AdminOverviewResponse overview = AdminOverviewResponse.builder().build();
        private List<AdminUserSearchResponse> searchResults = List.of();
        private AdminUserDetailsResponse details = AdminUserDetailsResponse.builder().build();
        private PersonalInfoResponse personalInfo = PersonalInfoResponse.builder().build();
        private List<AdminUserFeedbackResponse> feedback = List.of();
        private String lastEmail;
        private Long lastUserId;
        private String deletedEmail;
        private Long deletedUserId;

        private StubAdminApplicationService() {
            super(new NoopUserRepository(), new NoopChatRepository(), new NoopMessageRepository(),
                    new NoopFeedbackRepository(), new NoopCVRepository(), new NoopPersonalInfoRepository());
        }

        @Override
        public String getHealthMessage() {
            return "Admin access granted";
        }

        @Override
        public AdminOverviewResponse getOverview() {
            return overview;
        }

        @Override
        public List<AdminUserSearchResponse> searchUsersByEmail(String email) {
            lastEmail = email;
            return searchResults;
        }

        @Override
        public AdminUserDetailsResponse getUserDetails(Long userId) {
            lastUserId = userId;
            return details;
        }

        @Override
        public PersonalInfoResponse getUserPersonalInfo(Long userId) {
            lastUserId = userId;
            return personalInfo;
        }

        @Override
        public List<AdminUserFeedbackResponse> getUserFeedback(Long userId) {
            lastUserId = userId;
            return feedback;
        }

        @Override
        public void deleteUser(String actorEmail, Long userId) {
            deletedEmail = actorEmail;
            deletedUserId = userId;
        }
    }

    private static final class StubJwtFacade extends JwtFacade {
        private final String email;

        private StubJwtFacade(String email) {
            this.email = email;
        }

        @Override
        public String getAuthenticatedUserEmail() {
            return email;
        }
    }

    private static final class NoopUserRepository implements UserRepository {
        @Override public Optional<User> findById(Long id) { return Optional.empty(); }
        @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
        @Override public Optional<User> findByUsername(String username) { return Optional.empty(); }
        @Override public boolean existsByEmail(String email) { return false; }
        @Override public boolean existsByUsername(String username) { return false; }
        @Override public User save(User user) { return user; }
        @Override public void delete(User user) {}
        @Override public boolean deleteByEmail(String email) { return false; }
        @Override public boolean deleteByUsername(String username) { return false; }
        @Override public List<User> findAll() { return List.of(); }
        @Override public List<User> searchByEmail(String email) { return List.of(); }
        @Override public long count() { return 0L; }
        @Override public long countByRole(com.uniai.user.domain.valueobject.UserRole role) { return 0L; }
    }

    private static final class NoopChatRepository implements ChatRepository {
        @Override public Optional<Chat> findById(Long id) { return Optional.empty(); }
        @Override public List<Chat> findByUserUsernameOrderByUpdatedAtDesc(String username) { return List.of(); }
        @Override public String findTitleById(Long chatId) { return null; }
        @Override public Chat save(Chat chat) { return chat; }
        @Override public void delete(Chat chat) {}
        @Override public void deleteAll(List<Chat> chats) {}
        @Override public long count() { return 0L; }
        @Override public long countByUserId(Long userId) { return 0L; }
    }

    private static final class NoopMessageRepository implements MessageRepository {
        @Override public List<Message> findByChatIdOrderByTimestampAsc(Long chatId) { return List.of(); }
        @Override public List<Message> findTop10ByChatIdOrderByTimestampDesc(Long chatId) { return List.of(); }
        @Override public void deleteByChatId(Long chatId) {}
        @Override public void deleteByChatIdIn(List<Long> chatIds) {}
        @Override public long countByChatId(Long chatId) { return 0L; }
        @Override public long count() { return 0L; }
        @Override public long countByUserId(Long userId) { return 0L; }
        @Override public boolean existsByChatId(Long chatId) { return false; }
        @Override public Message save(Message message) { return message; }
    }

    private static final class NoopFeedbackRepository implements FeedbackRepository {
        @Override public Feedback save(Feedback feedback) { return feedback; }
        @Override public long count() { return 0L; }
        @Override public List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId) { return List.of(); }
        @Override public void deleteByUserId(Long userId) {}
    }

    private static final class NoopCVRepository implements CVRepository {
        @Override public Optional<CV> findById(Long id) { return Optional.empty(); }
        @Override public List<CV> findByUserId(Long userId) { return List.of(); }
        @Override public Optional<CV> findDefaultByUserId(Long userId) { return Optional.empty(); }
        @Override public long countByUserId(Long userId) { return 0L; }
        @Override public CV save(CV cv) { return cv; }
        @Override public void delete(CV cv) {}
        @Override public void deleteById(Long id) {}
    }

    private static final class NoopPersonalInfoRepository implements PersonalInfoRepository {
        @Override public Optional<PersonalInfo> findByUserId(Long userId) { return Optional.empty(); }
        @Override public PersonalInfo save(PersonalInfo personalInfo) { return personalInfo; }
    }
}
