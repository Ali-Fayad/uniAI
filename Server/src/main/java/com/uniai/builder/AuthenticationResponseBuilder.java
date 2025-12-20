package com.uniai.builder;

import com.uniai.dto.auth.AuthenticationResponseDto;
import com.uniai.dto.auth.SignUpDto;
import com.uniai.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.uniai.utils.ValidationUtils;

public class AuthenticationResponseBuilder {

    public static AuthenticationResponseDto getAuthenticationResponseDtoFromUser(User user) {
        AuthenticationResponseDto dto = AuthenticationResponseDto.builder()
                .username(user.getUsername().toLowerCase())
                .firstName(capitalize(user.getFirstName()))
                .lastName(capitalize(user.getLastName()))
                .email(user.getEmail().toLowerCase())
                .isVerified(user.isVerified())
                .isTwoFacAuth(user.isTwoFacAuth())
                .build();
        return dto;
    }

    public static User getUserFromSignUpDto(SignUpDto userDto, PasswordEncoder passwordEncoder) {
        User user = User.builder()
                .username(ValidationUtils.toLower(userDto.getUsername()))
                .firstName(ValidationUtils.capitalizeName(userDto.getFirstName()))
                .lastName(ValidationUtils.capitalizeName(userDto.getLastName()))
                .email(ValidationUtils.toLower(userDto.getEmail()))
                // Note: passwordEncoder.encode expects the server-side raw to hash; per rules
                // the backend must hash the received SHA-256 value again before storing.
                .password(passwordEncoder.encode(userDto.getPassword()))
                .build();
        return user;
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
