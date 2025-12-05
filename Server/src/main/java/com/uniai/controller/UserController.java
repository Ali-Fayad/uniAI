package com.uniai.controller;

import com.uniai.dto.ResponseDto;
import com.uniai.dto.SignInDto;
import com.uniai.dto.SignUpDto;
import com.uniai.services.UserService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

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
    public ResponseEntity<List<ResponseDto>> getAllUsers() {
        List<ResponseDto> entity = userService.getAllUsers();

        return ResponseEntity.ok(entity);
    }

    @GetMapping("auth/me")
    public ResponseEntity<ResponseDto> getMe(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        ResponseDto responseDto = userService.getResponseDtoByToken(token);
        return ResponseEntity.ok(responseDto);
    }

    private record TokenResponse(String token) {   }
}
