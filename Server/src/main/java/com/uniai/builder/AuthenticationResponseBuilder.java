package com.uniai.builder;

import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.dto.SignInDto;
import com.uniai.dto.SignUpDto;
import com.uniai.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.util.Collections;

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

    public static User getUserFromSignUpDto(SignUpDto userDto)
    {
        User user = User.builder()
                .username(userDto.getUsername().toLowerCase())
                .firstName(capitalize(userDto.getFirstName()))
                .lastName(capitalize(user.getLastName()))
                .email(userDto.getEmail().toLowerCase())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .build();
        return user;
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }



    public static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(HttpServletRequest request, AuthenticationResponseDto authenticatedUser) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList());
        authToken.setDetails( new WebAuthenticationDetailsSource().buildDetails(request));
        return authToken;
    }



}
