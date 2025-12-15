package com.uniai.controller;

import com.uniai.dto.GoogleAuthUrlRequestDto;
import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.dto.RequestPasswordDto;
import com.uniai.dto.EmailDto;
import com.uniai.dto.SignInDto;
import com.uniai.dto.SignUpDto;
import com.uniai.dto.VerifyDto;
import com.uniai.services.AuthService;
import com.uniai.services.OAuthGoogleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuthGoogleService oAuthGoogleService;

    @PostMapping("auth/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpDto signUpDto) {
        String token = authService.signUp(signUpDto);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("auth/signin")
    public ResponseEntity<?> signIn(@RequestBody SignInDto signInDto) {
        String token = authService.signIn(signInDto);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    //TODO : move Auth to rest filter
    @GetMapping("auth/me")
    public ResponseEntity<AuthenticationResponseDto> getMe(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        AuthenticationResponseDto responseDto = authService.getResponseDtoByToken(token);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("auth/verify")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyDto verifyDto) {
        String token = authService.verifyAndGenerateToken(verifyDto.getEmail(), verifyDto.getVerificationCode());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("auth/2fa/verify")
    public ResponseEntity<?> verifyTwoFactor(@Valid @RequestBody VerifyDto dto) {
        String token = authService.checkTwoFactorAndGenerate(dto.getEmail(), dto.getVerificationCode());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("auth/forget-password")
    public ResponseEntity<?> forgetPassword(@RequestBody EmailDto emailDto) {
        authService.forgetPassword(emailDto.getEmail());
        return ResponseEntity.ok(new MessageResponse("verification code sent"));
    }

    @PostMapping("auth/forget-password/confirm")
    public ResponseEntity<?> confirmForgetPassword(@RequestBody RequestPasswordDto dto) {
        String token = authService.resetPasswordWithCode(dto.getEmail(), dto.getVerificationCode(), dto.getNewPassword());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    /**
     * Accepts an optional request body with redirectUri and state.
     * If request is null the service uses the configured redirectUri.
     */
    @PostMapping("auth/google/url")
    public ResponseEntity<?> getGoogleAuthUrl(@Valid @RequestBody(required = false) GoogleAuthUrlRequestDto request) {
        String redirect = request == null ? null : request.getRedirectUri();
        String state = request == null ? null : request.getState();
        String url = oAuthGoogleService.getGoogleAuthorizationUrl(redirect, state);
        return ResponseEntity.ok(new UrlResponse(url));
    }

    private record TokenResponse(String token) {
    }

    private record MessageResponse(String message) {
    }

    private record UrlResponse(String url) {
    }
}