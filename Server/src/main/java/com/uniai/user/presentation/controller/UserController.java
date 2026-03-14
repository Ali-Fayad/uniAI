package com.uniai.user.presentation.controller;

import com.uniai.shared.infrastructure.jwt.JwtFacade;
import com.uniai.user.application.dto.command.ChangePasswordCommand;
import com.uniai.user.application.dto.command.DeleteUserCommand;
import com.uniai.user.application.dto.command.UpdateUserCommand;
import com.uniai.user.application.dto.response.AuthResponseDto;
import com.uniai.user.application.port.in.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles all user profile management endpoints.
 * Injects only the fine-grained use-case interfaces it actually calls (ISP).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final JwtFacade jwtFacade;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDto> getMe() {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(getCurrentUserUseCase.getMe(email));
    }

    @PutMapping("/me")
    public ResponseEntity<AuthResponseDto> updateMe(@Valid @RequestBody UpdateUserCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        return ResponseEntity.ok(updateUserUseCase.updateUser(email, command));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(@Valid @RequestBody DeleteUserCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        deleteUserUseCase.deleteUser(email, command);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordCommand command) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        changePasswordUseCase.changePassword(email, command);
        return ResponseEntity.ok().build();
    }
}
