package com.uniai.controller;

import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.dto.ChangePasswordDto;
import com.uniai.dto.EmailDto;
import com.uniai.dto.SignInDto;
import com.uniai.dto.SignUpDto;
import com.uniai.dto.VerifyDto;
import com.uniai.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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

    /**
     * Request a change-password code to be sent to the given email.
     * Body: { "email": "user@example.com" }
     */
    @PostMapping("auth/forget-password")
    public ResponseEntity<?> forgetPassword(@RequestBody EmailDto emailDto) {
        authService.forgetPassword(emailDto.getEmail());
        return ResponseEntity.ok(new MessageResponse("verification code sent"));
    }

    /**
     * Confirm change-password with email + code + newPassword.
     * Body: { "email": "...", "verificationCode": "...", "newPassword": "..." }
     * On success returns a new JWT for the user.
     */
    @PostMapping("auth/forget-password/confirm")
    public ResponseEntity<?> confirmForgetPassword(@RequestBody ChangePasswordDto dto) {
        String token = authService.resetPasswordWithCode(dto.getEmail(), dto.getVerificationCode(), dto.getNewPassword());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    private record TokenResponse(String token) {
    }

    private record MessageResponse(String message) {
    }
}
