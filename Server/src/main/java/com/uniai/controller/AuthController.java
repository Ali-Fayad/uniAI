package com.uniai.controller;

import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.dto.SignInDto;
import com.uniai.dto.SignUpDto;
import com.uniai.dto.VerifyDto;
import com.uniai.model.User;
import com.uniai.services.AuthService;
import com.uniai.services.EmailService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


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

    // @PostMapping("auth/forgetpassword")
    // public String changePassword(@RequestBody String entity) {
    //     //TODO: process POST request

    //     return entity;
    // }


    private record TokenResponse(String token) {
    }
}
