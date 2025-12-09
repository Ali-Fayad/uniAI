package com.uniai.controller;

import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.dto.SignInDto;
import com.uniai.dto.SignUpDto;
import com.uniai.services.AuthService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
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

    @GetMapping("auth/users")
    public ResponseEntity<List<AuthenticationResponseDto>> getAllUsers() {
        List<AuthenticationResponseDto> entity = authService.getAllUsers();

        return ResponseEntity.ok(entity);
    }

    @GetMapping("auth/me")
    public ResponseEntity<AuthenticationResponseDto> getMe(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        AuthenticationResponseDto responseDto = authService.getResponseDtoByToken(token);
        return ResponseEntity.ok(responseDto);
    }

    // @PostMapping")
    // public String postMethodName(@RequestBody String entity) {
    //     //TODO: process POST request

    //     return entity;
    // }



    private record TokenResponse(String token) {
    }
}
