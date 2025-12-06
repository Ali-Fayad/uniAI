package com.uniai.services;

import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.util.Collections;

public class AuthenticationResponseBuilder {


    public static AuthenticationResponseDto getAuthenticationResponseDtoFromUser(User user) {
        AuthenticationResponseDto dto = AuthenticationResponseDto.builder()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .isVerified(user.isVerified())
                .isTwoFacAuth(user.isTwoFacAuth())
                .build();
        return dto;
    }


    public static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(HttpServletRequest request, AuthenticationResponseDto authenticatedUser) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(authenticatedUser, null, Collections.emptyList());
        authToken.setDetails( new WebAuthenticationDetailsSource().buildDetails(request));
        return authToken;
    }



}
