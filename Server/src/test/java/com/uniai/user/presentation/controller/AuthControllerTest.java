package com.uniai.user.presentation.controller;

import com.uniai.user.application.dto.command.SignUpCommand;
import com.uniai.user.application.dto.command.EmailRequestCommand;
import com.uniai.user.application.dto.response.SignUpResultDto;
import com.uniai.user.application.port.in.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private AuthController authController;

    @Mock
    private SignUpUseCase signUpUseCase;

    @Mock
    private ResendVerificationCodeUseCase resendVerificationCodeUseCase;

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
    private CompleteGoogleLoginUseCase completeGoogleLoginUseCase;

    @Mock
    private CheckEmailAvailabilityUseCase checkEmailAvailabilityUseCase;

    @Mock
    private CheckUsernameAvailabilityUseCase checkUsernameAvailabilityUseCase;

    @BeforeEach
    void setUp() {
        authController = new AuthController(
                signUpUseCase,
                resendVerificationCodeUseCase,
                signInUseCase,
                verifyEmailUseCase,
                verifyTwoFactorUseCase,
                forgotPasswordUseCase,
                confirmPasswordResetUseCase,
                getGoogleAuthUrlUseCase,
                completeGoogleLoginUseCase,
                checkEmailAvailabilityUseCase,
                checkUsernameAvailabilityUseCase);
    }

    @Test
    void signUpShouldReturnAcceptedWithVerificationMessage() {
        when(signUpUseCase.signUp(any())).thenReturn(
                SignUpResultDto.verificationRequired("A verification code was sent — check your email!")
        );

        ResponseEntity<?> response = authController.signUp(new SignUpCommand());

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("A verification code was sent — check your email!", response.getBody());
    }

    @Test
    void resendVerificationCodeShouldReturnMessagePayload() throws Exception {
        when(resendVerificationCodeUseCase.resendVerificationCode(any())).thenReturn(
                "If verification is needed, a new code will be sent shortly."
        );

        ResponseEntity<?> response = authController.resendVerificationCode(new EmailRequestCommand("fresh@example.com"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Object body = response.getBody();
        assertEquals("If verification is needed, a new code will be sent shortly.",
                body.getClass().getMethod("message").invoke(body));
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
