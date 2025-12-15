package com.uniai.controller;

import com.uniai.dto.ChangePasswordDto;
import com.uniai.dto.DeleteAccountDto;
import com.uniai.dto.UpdateUserDto;
import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.services.UserService;
import com.uniai.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("users/me")
    public ResponseEntity<AuthenticationResponseDto> getMe(@RequestHeader("Authorization") String authHeader) {
        String token = jwtUtil.extractToken(authHeader);
        AuthenticationResponseDto tokenDto = jwtUtil.getUserDtoFromToken(token);
        String email = tokenDto.getEmail().toLowerCase();

        AuthenticationResponseDto response = userService.getMe(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("users/me")
    public ResponseEntity<AuthenticationResponseDto> updateMe(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateUserDto updateDto) {

        String token = jwtUtil.extractToken(authHeader);
        AuthenticationResponseDto tokenDto = jwtUtil.getUserDtoFromToken(token);
        String email = tokenDto.getEmail().toLowerCase();

        AuthenticationResponseDto response = userService.updateUserProfile(email, updateDto);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete current authenticated user (self delete).
     * Requires the user's current password in the request body for safety.
     * Returns 204 No Content on success.
     */
    @DeleteMapping("users/me")
    public ResponseEntity<Void> deleteMe(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DeleteAccountDto dto) {

        String token = jwtUtil.extractToken(authHeader);
        AuthenticationResponseDto tokenDto = jwtUtil.getUserDtoFromToken(token);
        String email = tokenDto.getEmail().toLowerCase();

        userService.deleteCurrentUser(email, dto.getPassword());

        return ResponseEntity.noContent().build();
    }

    /**
     * Change password for the currently authenticated user.
     * Body expects currentPassword and newPassword.
     */
    @PostMapping("users/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordDto dto) {

        String token = jwtUtil.extractToken(authHeader);
        AuthenticationResponseDto tokenDto = jwtUtil.getUserDtoFromToken(token);
        String email = tokenDto.getEmail().toLowerCase();

        userService.changePasswordForUser(email, dto.getCurrentPassword(), dto.getNewPassword());

        return ResponseEntity.ok().build();
    }
}