package com.uniai.user.application.service;

import com.uniai.shared.exception.AlreadyExistsException;
import com.uniai.shared.exception.VerificationCodeRateLimitException;
import com.uniai.shared.infrastructure.jwt.JwtUtil;
import com.uniai.user.application.dto.command.EmailRequestCommand;
import com.uniai.user.application.dto.command.SignUpCommand;
import com.uniai.user.application.dto.response.SignUpResultDto;
import com.uniai.user.application.port.out.NotificationPort;
import com.uniai.user.application.port.out.OAuthPort;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.model.VerifyCode;
import com.uniai.user.domain.repository.UserRepository;
import com.uniai.user.domain.repository.VerifyCodeRepository;
import com.uniai.user.domain.valueobject.UserRole;
import com.uniai.user.domain.valueobject.VerificationCodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerifyCodeRepository verifyCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private NotificationPort notificationPort;

    @Mock
    private OAuthPort oAuthPort;

    @InjectMocks
    private AuthApplicationService authApplicationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authApplicationService, "codeLength", 6);
        ReflectionTestUtils.setField(authApplicationService, "codeExpiryMinutes", 15);
    }

    @Test
    void signUpShouldReturnConflictMessageWhenEmailAlreadyExists() {
        SignUpCommand command = new SignUpCommand();
        command.setEmail("used@example.com");
        command.setUsername("newuser");
        command.setPassword("Password123");

        when(userRepository.existsByEmail("used@example.com")).thenReturn(true);

        AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> authApplicationService.signUp(command)
        );

        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    void signUpShouldBootstrapFirstUserAsAdmin() {
        SignUpCommand command = new SignUpCommand();
        command.setFirstName("Alice");
        command.setLastName("Admin");
        command.setUsername("alice");
        command.setEmail("alice@example.com");
        command.setPassword("Password123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });
        when(verifyCodeRepository.save(any(VerifyCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SignUpResultDto result = authApplicationService.signUp(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(UserRole.ADMIN, userCaptor.getValue().getRole());
        assertTrue(result.verificationRequired());
        assertEquals("A verification code was sent — check your email!", result.message());
        verify(verifyCodeRepository).markByUserIdAndTypeUsed(eq(1L), eq(VerificationCodeType.REGISTRATION));
        verify(verifyCodeRepository).save(any(VerifyCode.class));
        verify(notificationPort).sendVerificationEmail(eq("alice@example.com"), eq(VerificationCodeType.REGISTRATION), org.mockito.ArgumentMatchers.anyString());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void signUpShouldCreateLaterUsersAsUser() {
        SignUpCommand command = new SignUpCommand();
        command.setFirstName("Bob");
        command.setLastName("User");
        command.setUsername("bob");
        command.setEmail("bob@example.com");
        command.setPassword("Password123");

        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.count()).thenReturn(1L);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(2L);
                    return user;
                });
        when(verifyCodeRepository.save(any(VerifyCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SignUpResultDto result = authApplicationService.signUp(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(UserRole.USER, userCaptor.getValue().getRole());
        assertTrue(result.verificationRequired());
        assertEquals("A verification code was sent — check your email!", result.message());
        verify(verifyCodeRepository).markByUserIdAndTypeUsed(eq(2L), eq(VerificationCodeType.REGISTRATION));
        verify(notificationPort).sendVerificationEmail(eq("bob@example.com"), eq(VerificationCodeType.REGISTRATION), org.mockito.ArgumentMatchers.anyString());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void resendVerificationCodeShouldAllowFirstManualResendImmediatelyAfterSignup() {
        User user = User.builder()
                .id(10L)
                .email("fresh@example.com")
                .isVerified(false)
                .build();
        VerifyCode latest = VerifyCode.builder()
                .userId(10L)
                .code("ABC123")
                .type(VerificationCodeType.REGISTRATION)
                .createdAt(java.time.LocalDateTime.now().minusSeconds(10))
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(userRepository.findByEmail("fresh@example.com")).thenReturn(java.util.Optional.of(user));
        when(verifyCodeRepository.findTopByUserIdAndType(10L, VerificationCodeType.REGISTRATION)).thenReturn(java.util.Optional.of(latest));
        when(verifyCodeRepository.existsByUserIdAndTypeAndUsedTrue(10L, VerificationCodeType.REGISTRATION)).thenReturn(false);
        when(verifyCodeRepository.save(any(VerifyCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String message = authApplicationService.resendVerificationCode(new EmailRequestCommand("fresh@example.com"));

        assertEquals("If verification is needed, a new code will be sent shortly.", message);
        verify(verifyCodeRepository).markByUserIdAndTypeUsed(eq(10L), eq(VerificationCodeType.REGISTRATION));
        verify(notificationPort).sendVerificationEmail(eq("fresh@example.com"), eq(VerificationCodeType.REGISTRATION), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void resendVerificationCodeShouldSendWhenCooldownExpired() {
        User user = User.builder()
                .id(10L)
                .email("fresh@example.com")
                .isVerified(false)
                .build();
        VerifyCode latest = VerifyCode.builder()
                .userId(10L)
                .code("ABC123")
                .type(VerificationCodeType.REGISTRATION)
                .createdAt(java.time.LocalDateTime.now().minusMinutes(2))
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(userRepository.findByEmail("fresh@example.com")).thenReturn(java.util.Optional.of(user));
        when(verifyCodeRepository.findTopByUserIdAndType(10L, VerificationCodeType.REGISTRATION)).thenReturn(java.util.Optional.of(latest));
        when(verifyCodeRepository.existsByUserIdAndTypeAndUsedTrue(10L, VerificationCodeType.REGISTRATION)).thenReturn(true);
        when(verifyCodeRepository.save(any(VerifyCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String message = authApplicationService.resendVerificationCode(new EmailRequestCommand("fresh@example.com"));

        assertEquals("If verification is needed, a new code will be sent shortly.", message);
        verify(verifyCodeRepository).markByUserIdAndTypeUsed(eq(10L), eq(VerificationCodeType.REGISTRATION));
        verify(notificationPort).sendVerificationEmail(eq("fresh@example.com"), eq(VerificationCodeType.REGISTRATION), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void resendVerificationCodeShouldRateLimitWithinOneMinute() {
        User user = User.builder()
                .id(11L)
                .email("fresh2@example.com")
                .isVerified(false)
                .build();
        VerifyCode latest = VerifyCode.builder()
                .userId(11L)
                .code("ABC123")
                .type(VerificationCodeType.REGISTRATION)
                .createdAt(java.time.LocalDateTime.now())
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(userRepository.findByEmail("fresh2@example.com")).thenReturn(java.util.Optional.of(user));
        when(verifyCodeRepository.findTopByUserIdAndType(11L, VerificationCodeType.REGISTRATION)).thenReturn(java.util.Optional.of(latest));
        when(verifyCodeRepository.existsByUserIdAndTypeAndUsedTrue(11L, VerificationCodeType.REGISTRATION)).thenReturn(true);

        VerificationCodeRateLimitException exception = assertThrows(
                VerificationCodeRateLimitException.class,
                () -> authApplicationService.resendVerificationCode(new EmailRequestCommand("fresh2@example.com"))
        );

        assertEquals("Please wait before requesting another verification code.", exception.getMessage());
        verifyNoInteractions(notificationPort);
    }

    @Test
    void resendVerificationCodeShouldReturnGenericSuccessForUnknownOrVerifiedUsers() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(java.util.Optional.empty());
        String unknownMessage = authApplicationService.resendVerificationCode(new EmailRequestCommand("unknown@example.com"));
        assertEquals("If verification is needed, a new code will be sent shortly.", unknownMessage);

        User verifiedUser = User.builder()
                .id(12L)
                .email("verified@example.com")
                .isVerified(true)
                .build();
        when(userRepository.findByEmail("verified@example.com")).thenReturn(java.util.Optional.of(verifiedUser));
        String verifiedMessage = authApplicationService.resendVerificationCode(new EmailRequestCommand("verified@example.com"));
        assertEquals("If verification is needed, a new code will be sent shortly.", verifiedMessage);

        verifyNoInteractions(notificationPort);
    }

    @Test
    void isEmailAvailableShouldReturnFalseWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("used@example.com")).thenReturn(true);

        boolean available = authApplicationService.isEmailAvailable("used@example.com");

        assertFalse(available);
    }
}
