package com.uniai.admin.application.service;

import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.model.Message;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.domain.repository.MessageRepository;
import com.uniai.cvbuilder.domain.model.CV;
import com.uniai.cvbuilder.domain.model.PersonalInfo;
import com.uniai.cvbuilder.domain.repository.CVRepository;
import com.uniai.cvbuilder.domain.repository.PersonalInfoRepository;
import com.uniai.feedback.domain.model.Feedback;
import com.uniai.feedback.domain.repository.FeedbackRepository;
import com.uniai.shared.exception.LastAdminProtectionException;
import com.uniai.support.PostgresIntegrationTest;
import com.uniai.user.domain.model.VerifyCode;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import com.uniai.user.domain.repository.VerifyCodeRepository;
import com.uniai.user.domain.valueobject.UserRole;
import com.uniai.user.domain.valueobject.VerificationCodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AdminPersistenceIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private AdminApplicationService adminApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private CVRepository cvRepository;

    @Autowired
    private PersonalInfoRepository personalInfoRepository;

    @Autowired
    private VerifyCodeRepository verifyCodeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE messages, chats, feedback, verification_code, personal_info, cvs, users
                RESTART IDENTITY CASCADE
                """);
    }

    @Test
    void adminDeleteShouldRemoveFeedbackAndCascadeUserOwnedData() {
        User admin = userRepository.save(user("admin@example.com", "admin", UserRole.ADMIN));
        User target = userRepository.save(user("target@example.com", "target", UserRole.USER));

        feedbackRepository.save(feedback(target.getId(), "Target feedback"));
        personalInfoRepository.save(PersonalInfo.builder()
                .userId(target.getId())
                .phone("123")
                .summary("Summary")
                .isFilled(true)
                .educationJson("[]")
                .skillsJson("[]")
                .languagesJson("[]")
                .experienceJson("[]")
                .projectsJson("[]")
                .certificatesJson("[]")
                .build());
        cvRepository.save(CV.builder()
                .userId(target.getId())
                .cvName("Target CV")
                .sectionsOrder(List.of())
                .isDefault(true)
                .build());
        Chat chat = chatRepository.save(Chat.builder()
                .user(target)
                .title("Target chat")
                .build());
        messageRepository.save(Message.builder()
                .chat(chat)
                .senderId(target.getId())
                .content("Target message")
                .timestamp(LocalDateTime.now())
                .build());
        verifyCodeRepository.save(VerifyCode.builder()
                .userId(target.getId())
                .code("123456")
                .type(VerificationCodeType.REGISTRATION)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build());

        adminApplicationService.deleteUser(admin.getEmail(), target.getId());

        assertTrue(userRepository.findById(target.getId()).isEmpty());
        assertTrue(feedbackRepository.findByUserIdOrderByCreatedAtDesc(target.getId()).isEmpty());
        assertTrue(personalInfoRepository.findByUserId(target.getId()).isEmpty());
        assertEquals(0L, cvRepository.countByUserId(target.getId()));
        assertEquals(0L, chatRepository.countByUserId(target.getId()));
        assertEquals(0L, messageRepository.countByUserId(target.getId()));
        assertTrue(verifyCodeRepository.findTopByUserIdAndType(target.getId(), VerificationCodeType.REGISTRATION).isEmpty());
    }

    @Test
    void roleUpdateShouldPromoteAndDemoteUsersPersistently() {
        User actor = userRepository.save(user("actor@example.com", "actor", UserRole.USER));
        User target = userRepository.save(user("target@example.com", "target", UserRole.USER));
        User secondAdmin = userRepository.save(user("second-admin@example.com", "secondadmin", UserRole.ADMIN));

        assertEquals(UserRole.ADMIN.name(),
                adminApplicationService.updateUserRole(actor.getEmail(), target.getId(), UserRole.ADMIN).getRole());
        assertEquals(UserRole.ADMIN, userRepository.findById(target.getId()).orElseThrow().getRole());

        assertEquals(UserRole.USER.name(),
                adminApplicationService.updateUserRole(secondAdmin.getEmail(), target.getId(), UserRole.USER).getRole());
        assertEquals(UserRole.USER, userRepository.findById(target.getId()).orElseThrow().getRole());
    }

    @Test
    void lastAdminDemotionShouldBeBlockedWithRealRepositories() {
        User actor = userRepository.save(user("actor@example.com", "actor", UserRole.USER));
        User soleAdmin = userRepository.save(user("sole-admin@example.com", "soleadmin", UserRole.ADMIN));

        assertThrows(LastAdminProtectionException.class,
                () -> adminApplicationService.updateUserRole(actor.getEmail(), soleAdmin.getId(), UserRole.USER));
    }

    private static User user(String email, String username, UserRole role) {
        return User.builder()
                .email(email)
                .username(username)
                .password("encoded-password")
                .role(role)
                .isVerified(true)
                .isTwoFacAuth(false)
                .build();
    }

    private static Feedback feedback(Long userId, String content) {
        return Feedback.builder()
                .userId(userId)
                .content(content)
                .rating(5)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
