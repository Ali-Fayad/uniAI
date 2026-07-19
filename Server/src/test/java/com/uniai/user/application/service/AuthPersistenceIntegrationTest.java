package com.uniai.user.application.service;

import com.uniai.shared.infrastructure.jwt.JwtTokenPayload;
import com.uniai.shared.infrastructure.jwt.JwtUtil;
import com.uniai.support.PostgresIntegrationTest;
import com.uniai.user.application.dto.command.SignUpCommand;
import com.uniai.user.application.dto.command.VerifyCommand;
import com.uniai.user.application.dto.response.SignUpResultDto;
import com.uniai.user.application.port.out.NotificationPort;
import com.uniai.user.application.port.out.OAuthPort;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.model.VerifyCode;
import com.uniai.user.domain.repository.UserRepository;
import com.uniai.user.domain.repository.VerifyCodeRepository;
import com.uniai.user.domain.valueobject.VerificationCodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(AuthPersistenceIntegrationTest.TestBeans.class)
class AuthPersistenceIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private AuthApplicationService authApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerifyCodeRepository verifyCodeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NotificationPort notificationPort;

    @Autowired
    private OAuthPort oAuthPort;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE verification_code, users
                RESTART IDENTITY CASCADE
                """);
    }

    @Test
    void signUpShouldPersistVerificationCodeAndAllowEmailVerification() {
        SignUpCommand command = new SignUpCommand();
        command.setFirstName("Fresh");
        command.setLastName("User");
        command.setUsername("freshuser");
        command.setEmail("fresh@example.com");
        command.setPassword("Password123");

        SignUpResultDto result = authApplicationService.signUp(command);

        assertTrue(result.verificationRequired());
        assertEquals("A verification code was sent — check your email!", result.message());

        User user = userRepository.findByEmail("fresh@example.com").orElseThrow();
        assertFalse(user.isVerified());

        VerifyCode verifyCode = verifyCodeRepository
                .findTopByUserIdAndType(user.getId(), VerificationCodeType.REGISTRATION)
                .orElseThrow();

        assertEquals(user.getId(), verifyCode.getUserId());
        assertFalse(verifyCode.isUsed());
        assertNotNull(verifyCode.getCode());

        String token = authApplicationService.verifyEmail(new VerifyCommand("fresh@example.com", verifyCode.getCode()));

        assertNotNull(token);
        assertTrue(userRepository.findByEmail("fresh@example.com").orElseThrow().isVerified());

        Optional<VerifyCode> usedCode = verifyCodeRepository.findTopByUserIdAndType(user.getId(), VerificationCodeType.REGISTRATION);
        assertTrue(usedCode.orElseThrow().isUsed());
    }

    @TestConfiguration
    static class TestBeans {
        @Bean
        @Primary
        PasswordEncoder passwordEncoder() {
            return new PasswordEncoder() {
                @Override
                public String encode(CharSequence rawPassword) {
                    return "encoded-password";
                }

                @Override
                public boolean matches(CharSequence rawPassword, String encodedPassword) {
                    return encodedPassword != null && encodedPassword.equals("encoded-password");
                }
            };
        }

        @Bean
        @Primary
        JwtUtil jwtUtil() {
            return new JwtUtil() {
                @Override
                public String generateToken(JwtTokenPayload payload) {
                    return "jwt-token";
                }

                @Override
                public String generateToken(String username) {
                    return "jwt-token";
                }
            };
        }

        @Bean
        @Primary
        NotificationPort notificationPort() {
            return new NotificationPort() {
                @Override
                public void sendVerificationEmail(String toEmail, VerificationCodeType type, String code) {
                    // no-op for integration test
                }
            };
        }

        @Bean
        @Primary
        OAuthPort oAuthPort() {
            return new OAuthPort() {
                @Override
                public String buildAuthorizationUrl(String overrideRedirectUri, String state) {
                    return "https://example.com/oauth";
                }

                @Override
                public OAuthPort.GoogleProfile authenticate(String code, String redirectUri) {
                    return null;
                }
            };
        }
    }
}
