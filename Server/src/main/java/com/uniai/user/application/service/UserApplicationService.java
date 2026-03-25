package com.uniai.user.application.service;

import com.uniai.shared.exception.AlreadyExistsException;
import com.uniai.shared.exception.InvalidEmailOrPassword;
import com.uniai.user.application.dto.command.ChangePasswordCommand;
import com.uniai.user.application.dto.command.DeleteUserCommand;
import com.uniai.user.application.dto.command.UpdateUserCommand;
import com.uniai.user.application.dto.response.AuthResponseDto;
import com.uniai.user.application.port.in.ChangePasswordUseCase;
import com.uniai.user.application.port.in.DeleteUserUseCase;
import com.uniai.user.application.port.in.GetCurrentUserUseCase;
import com.uniai.user.application.port.in.UpdateUserUseCase;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for user profile management use cases.
 */
@Service
@RequiredArgsConstructor
public class UserApplicationService implements
        GetCurrentUserUseCase,
        UpdateUserUseCase,
        DeleteUserUseCase,
        ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // -------------------------------------------------------------------------
    // GetCurrentUserUseCase
    // -------------------------------------------------------------------------

    @Override
    public AuthResponseDto getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidEmailOrPassword::new);
        return toDto(user);
    }

    // -------------------------------------------------------------------------
    // UpdateUserUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AuthResponseDto updateUser(String email, UpdateUserCommand command) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidEmailOrPassword::new);

        if (command.getUsername() != null && !command.getUsername().isBlank()) {
            String requestedUsername = command.getUsername().toLowerCase();
            if (!requestedUsername.equals(user.getUsername())) {
                if (userRepository.existsByUsername(requestedUsername)) {
                    throw new AlreadyExistsException("Username already exists");
                }
                user.setUsername(requestedUsername);
            }
        }

        if (command.getEmail() != null && !command.getEmail().isBlank()) {
            String requestedEmail = command.getEmail().toLowerCase();
            if (!requestedEmail.equals(user.getEmail())) {
                if (userRepository.existsByEmail(requestedEmail)) {
                    throw new AlreadyExistsException("Email already registered");
                }
                user.setEmail(requestedEmail);
            }
        }

        if (command.getFirstName() != null) {
            user.setFirstName(command.getFirstName());
        }
        if (command.getLastName() != null) {
            user.setLastName(command.getLastName());
        }
        if (command.getEnableTwoFactor() != null) {
            user.setTwoFacAuth(command.getEnableTwoFactor());
        }

        userRepository.save(user);
        return toDto(user);
    }

    // -------------------------------------------------------------------------
    // DeleteUserUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void deleteUser(String email, DeleteUserCommand command) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidEmailOrPassword::new);

        if (!passwordEncoder.matches(command.getPassword(), user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        userRepository.delete(user);
    }

    // -------------------------------------------------------------------------
    // ChangePasswordUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordCommand command) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidEmailOrPassword::new);

        if (!passwordEncoder.matches(command.getCurrentPassword(), user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        user.setPassword(passwordEncoder.encode(command.getNewPassword()));
        userRepository.save(user);
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private AuthResponseDto toDto(User user) {
        return AuthResponseDto.builder()
                .username(user.getUsername().toLowerCase())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail().toLowerCase())
                .isVerified(user.isVerified())
                .isTwoFacAuth(user.isTwoFacAuth())
                .build();
    }
}
