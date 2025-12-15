package com.uniai.controller;

import com.uniai.dto.auth.AuthenticationResponseDto;
import com.uniai.dto.user.ChangePasswordDto;
import com.uniai.dto.user.DeleteAccountDto;
import com.uniai.dto.user.UpdateUserDto;
import com.uniai.security.jwt.JwtFacade;
import com.uniai.services.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtFacade authenticationFacade;

    @GetMapping("users/me")
    public ResponseEntity<AuthenticationResponseDto> getMe() {
        String email = authenticationFacade.getAuthenticatedUserEmail();
        AuthenticationResponseDto response = userService.getMe(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("users/me")
    public ResponseEntity<AuthenticationResponseDto> updateMe(
            @Valid @RequestBody UpdateUserDto updateDto) {

        String email = authenticationFacade.getAuthenticatedUserEmail();
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
            @Valid @RequestBody DeleteAccountDto dto) {

        String email = authenticationFacade.getAuthenticatedUserEmail();
        userService.deleteCurrentUser(email, dto.getPassword());

        return ResponseEntity.noContent().build();
    }

    /**
     * Change password for the currently authenticated user.
     * Body expects currentPassword and newPassword.
     */
    @PostMapping("users/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordDto dto) {

        String email = authenticationFacade.getAuthenticatedUserEmail();
        userService.changePasswordForUser(email, dto.getCurrentPassword(), dto.getNewPassword());

        return ResponseEntity.ok().build();
    }
}
