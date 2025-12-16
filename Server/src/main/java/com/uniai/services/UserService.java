package com.uniai.services;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uniai.builder.AuthenticationResponseBuilder;
import com.uniai.dto.auth.AuthenticationResponseDto;
import com.uniai.dto.user.UpdateUserDto;
import com.uniai.exception.AlreadyExistsException;
import com.uniai.exception.InvalidEmailOrPassword;
import com.uniai.model.User;
import com.uniai.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public List<AuthenticationResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(AuthenticationResponseBuilder::getAuthenticationResponseDtoFromUser)
                .toList();
    }

    public void deleteUserByEmail(String email) {
        boolean deleted = userRepository.deleteByEmail(email);
        if (!deleted) {
            throw new RuntimeException("User not found with email: " + email);
        }
    }

    /**
     * Returns the AuthenticationResponseDto for the current authenticated user.
     * Controller extracts token and passes the email here.
     */
    public AuthenticationResponseDto getMe(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new InvalidEmailOrPassword();
        }
        return AuthenticationResponseBuilder.getAuthenticationResponseDtoFromUser(user);
    }

    /**
     * Update the user's profile fields. Username uniqueness validated here.
     * email is the authenticated user's email extracted by the controller.
     */
    @Transactional
    public AuthenticationResponseDto updateUserProfile(String email, UpdateUserDto updateDto) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new InvalidEmailOrPassword();
        }

        // Username uniqueness check
        if (updateDto.getUsername() != null && !updateDto.getUsername().isBlank()) {
            String requestedUsername = updateDto.getUsername().toLowerCase();
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
            // Optionally require a confirmation step when enabling 2FA.
        }

        userRepository.save(user);

        return AuthenticationResponseBuilder.getAuthenticationResponseDtoFromUser(user);
    }

    /**
     * Delete the currently authenticated user after verifying password.
     */
    @Transactional
    public void deleteCurrentUser(String email, String currentPassword) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new InvalidEmailOrPassword();
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        userRepository.delete(user); // Hard delete; swap for soft-delete if preferred
    }

    /**
     * Change password for the current authenticated user after verifying current password.
     */
    @Transactional
    public void changePasswordForUser(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new InvalidEmailOrPassword();
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Fix: previous method used findByUsername with an email parameter — corrected to findByEmail.
     */
    public void changePasswordWithoutOTP(String email, String Password, String newPassword) {
        User user = userRepository.findByEmail(email);

        if (user == null || !passwordEncoder.matches(Password, user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
