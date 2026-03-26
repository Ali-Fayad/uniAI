package com.uniai.user.presentation.controller;

import com.uniai.user.application.port.in.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private AuthController authController;

    @Mock
    private SignUpUseCase signUpUseCase;

    @Mock
    private SignInUseCase signInUseCase;

    @Mock
    private VerifyEmailUseCase verifyEmailUseCase;

    @Mock
    private VerifyTwoFactorUseCase verifyTwoFactorUseCase;

    @Mock
    private ForgotPasswordUseCase forgotPasswordUseCase;

    @Mock
    private ConfirmPasswordResetUseCase confirmPasswordResetUseCase;

    @Mock
    private GetGoogleAuthUrlUseCase getGoogleAuthUrlUseCase;

    @Mock
    private CheckEmailAvailabilityUseCase checkEmailAvailabilityUseCase;

    @Mock
    private CheckUsernameAvailabilityUseCase checkUsernameAvailabilityUseCase;

    @BeforeEach
    void setUp() {
        authController = new AuthController(
                signUpUseCase,
                signInUseCase,
                verifyEmailUseCase,
                verifyTwoFactorUseCase,
                forgotPasswordUseCase,
                confirmPasswordResetUseCase,
                getGoogleAuthUrlUseCase,
                checkEmailAvailabilityUseCase,
                checkUsernameAvailabilityUseCase);
    }

    @Test
    void checkEmailShouldReturnAvailabilityPayload() throws Exception {
        when(checkEmailAvailabilityUseCase.isEmailAvailable(eq("new@example.com"))).thenReturn(true);

        ResponseEntity<?> response = authController.checkEmail("new@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Object body = response.getBody();
        assertEquals(true, body.getClass().getMethod("available").invoke(body));
        assertEquals("Email available", body.getClass().getMethod("message").invoke(body));
    }

    @Test
    void checkUsernameShouldReturnAvailabilityPayload() throws Exception {
        when(checkUsernameAvailabilityUseCase.isUsernameAvailable(eq("new_username"))).thenReturn(true);

        ResponseEntity<?> response = authController.checkUsername("new_username");

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Object body = response.getBody();
        assertEquals(true, body.getClass().getMethod("available").invoke(body));
        assertEquals("Username available", body.getClass().getMethod("message").invoke(body));
    }
}
