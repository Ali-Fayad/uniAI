package com.uniai.controller;

import com.uniai.builder.AuthenticationResponseBuilder;
import com.uniai.dto.ChangePasswordDto;
import com.uniai.dto.DeleteAccountDto;
import com.uniai.dto.UpdateUserDto;
import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.exception.AlreadyExistsException;
import com.uniai.exception.InvalidEmailOrPassword;
import com.uniai.model.User;
import com.uniai.repository.UserRepository;
import com.uniai.services.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * User endpoints for "me" operations.
 *
 * Routes:
 *  - GET    /api/users/me
 *  - PUT    /api/users/me
 *  - DELETE /api/users/me
 *  - POST   /api/users/change-password
 *
 * Notes:
 *  - All endpoints require an Authorization header with a Bearer token.
 *  - The controller uses the email inside the token (via AuthService.getResponseDtoByToken)
 *    to identify the target user and enforces that the token user operates on their own account.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidEmailOrPassword(); // reuse existing exception for unauthorized-like cases
        }
        return authHeader.substring(7);
    }

    /**
     * Get the current authenticated user's profile.
     */
    @GetMapping("users/me")
    public ResponseEntity<AuthenticationResponseDto> getMe(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        AuthenticationResponseDto dto = authService.getResponseDtoByToken(token);
        return ResponseEntity.ok(dto);
    }

    /**
     * Update current authenticated user.
     * Accepts partial updates via UpdateUserDto (nullable fields).
     *
     * Behavior:
     *  - Fetches the user by email from the token and applies only non-null fields from the DTO.
     *  - If username is provided, checks uniqueness (excluding the current user's username).
     *  - Saves the user and returns the fresh AuthenticationResponseDto built from the saved user.
     *
     * Note: email cannot be changed here.
     */
    @PutMapping("users/me")
    public ResponseEntity<AuthenticationResponseDto> updateMe(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateUserDto updateDto) {

        String token = extractToken(authHeader);
        AuthenticationResponseDto tokenDto = authService.getResponseDtoByToken(token);
        String email = tokenDto.getEmail().toLowerCase();

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new InvalidEmailOrPassword();
        }

        // Username uniqueness check
        if (updateDto.getUsername() != null && !updateDto.getUsername().isBlank()) {
            String requestedUsername = updateDto.getUsername().toLowerCase();
            // If requested username differs from current, check existence
            if (!requestedUsername.equals(user.getUsername())) {
                if (userRepository.existsByUsername(requestedUsername)) {
                    throw new AlreadyExistsException("Username already exists");
                }
                user.setUsername(requestedUsername);
            }
        }

        if (updateDto.getFirstName() != null) {
            user.setFirstName(updateDto.getFirstName());
        }
        if (updateDto.getLastName() != null) {
            user.setLastName(updateDto.getLastName());
        }

        if (updateDto.getEnableTwoFactor() != null) {
            user.setTwoFacAuth(updateDto.getEnableTwoFactor());
            // Recommended: require a confirmation step when enabling 2FA.
            // This implementation follows your instruction to allow toggling in the PUT request.
        }

        userRepository.save(user);

        // Return the up-to-date DTO built from saved user
        AuthenticationResponseDto responseDto = AuthenticationResponseBuilder
                .getAuthenticationResponseDtoFromUser(user);

        return ResponseEntity.ok(responseDto);
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

        String token = extractToken(authHeader);
        AuthenticationResponseDto tokenDto = authService.getResponseDtoByToken(token);
        String email = tokenDto.getEmail().toLowerCase();

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new InvalidEmailOrPassword();
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        // Hard delete. If you prefer soft-delete, replace with soft-delete logic.
        userRepository.delete(user);

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

        String token = extractToken(authHeader);
        AuthenticationResponseDto tokenDto = authService.getResponseDtoByToken(token);
        String email = tokenDto.getEmail().toLowerCase();

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new InvalidEmailOrPassword();
        }

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
