package com.uniai.controller;

import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.dto.EmailRequestDto;
import com.uniai.dto.SignInDto;
import com.uniai.dto.SignUpDto;
import com.uniai.services.EmailService;
import com.uniai.services.UserService;

import java.io.IOException;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("auth/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpDto signUpDto) {
        String token = userService.signUp(signUpDto);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("auth/signin")
    public ResponseEntity<?> signIn(@RequestBody SignInDto signInDto) {
        String token = userService.signIn(signInDto);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @GetMapping("auth/users")
    public ResponseEntity<List<AuthenticationResponseDto>> getAllUsers() {
        List<AuthenticationResponseDto> entity = userService.getAllUsers();

        return ResponseEntity.ok(entity);
    }

    @GetMapping("auth/me")
    public ResponseEntity<AuthenticationResponseDto> getMe(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        AuthenticationResponseDto responseDto = userService.getResponseDtoByToken(token);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("auth/verify")
    public ResponseEntity<?> sendVerificationCode(@RequestBody EmailRequestDto request)
            throws MessagingException, IOException {

        emailService.sendVerificationCode(request.email());
        return ResponseEntity.ok(new MessageResponse("Verification email sent successfully."));
    }

    private record MessageResponse(String message) {
    }

    private record TokenResponse(String token) {
    }
}
