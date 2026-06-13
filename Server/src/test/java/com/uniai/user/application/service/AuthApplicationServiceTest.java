package com.uniai.user.application.service;

import com.uniai.shared.exception.AlreadyExistsException;
import com.uniai.shared.infrastructure.jwt.JwtUtil;
import com.uniai.user.application.dto.command.SignUpCommand;
import com.uniai.user.application.port.out.NotificationPort;
import com.uniai.user.application.port.out.OAuthPort;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import com.uniai.user.domain.repository.VerifyCodeRepository;
import com.uniai.user.domain.valueobject.UserRole;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(
                com.uniai.shared.exception.VerificationNeededException.class,
                () -> authApplicationService.signUp(command)
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(UserRole.ADMIN, userCaptor.getValue().getRole());
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
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(
                com.uniai.shared.exception.VerificationNeededException.class,
                () -> authApplicationService.signUp(command)
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(UserRole.USER, userCaptor.getValue().getRole());
    }

    @Test
    void isEmailAvailableShouldReturnFalseWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("used@example.com")).thenReturn(true);

        boolean available = authApplicationService.isEmailAvailable("used@example.com");

        assertFalse(available);
    }
}
