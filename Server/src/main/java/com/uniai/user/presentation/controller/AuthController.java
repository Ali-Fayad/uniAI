package com.uniai.user.presentation.controller;

import com.uniai.user.application.dto.command.RequestPasswordCommand;
import com.uniai.user.application.dto.command.SignInCommand;
import com.uniai.user.application.dto.command.SignUpCommand;
import com.uniai.user.application.dto.command.VerifyCommand;
import com.uniai.user.application.port.in.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles all authentication endpoints.
 * Injects only the fine-grained use-case interfaces it actually calls (ISP).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignUpUseCase signUpUseCase;
    private final SignInUseCase signInUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final VerifyTwoFactorUseCase verifyTwoFactorUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
    private final GetGoogleAuthUrlUseCase getGoogleAuthUrlUseCase;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpCommand command) {
        String token = signUpUseCase.signUp(command);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInCommand command) {
        String token = signInUseCase.signIn(command);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyCommand command) {
        String token = verifyEmailUseCase.verifyEmail(command);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verifyTwoFactor(@Valid @RequestBody VerifyCommand command) {
        String token = verifyTwoFactorUseCase.verifyTwoFactor(command);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.forgotPassword(request.email());
        return ResponseEntity.ok(new MessageResponse("Verification code sent"));
    }

    @PostMapping("/forget-password/confirm")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody RequestPasswordCommand command) {
        String token = confirmPasswordResetUseCase.confirmPasswordReset(command);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/google/url")
    public ResponseEntity<?> getGoogleAuthUrl(@Valid @RequestBody(required = false) GoogleAuthUrlRequest request) {
        String redirectUri = request == null ? null : request.redirectUri();
        String state = request == null ? null : request.state();
        String url = getGoogleAuthUrlUseCase.getGoogleAuthUrl(redirectUri, state);
        return ResponseEntity.ok(new UrlResponse(url));
    }

    // -------------------------------------------------------------------------
    // Records for request/response bodies
    // -------------------------------------------------------------------------

    private record TokenResponse(String token) {}
    private record MessageResponse(String message) {}
    private record UrlResponse(String url) {}
    private record ForgotPasswordRequest(String email) {}

    private record GoogleAuthUrlRequest(
            @jakarta.validation.constraints.Size(max = 2048) String redirectUri,
            @jakarta.validation.constraints.Size(max = 512) String state) {}
}
