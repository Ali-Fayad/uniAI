package com.uniai.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.uniai.builder.OAuthGoogleBuilder;
import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.model.User;
import com.uniai.repository.UserRepository;
import com.uniai.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Orchestrates Google OAuth flow at a high level.
 * Each method does one job and delegates work to OAuthGoogleBuilder.
 */
@Service
@RequiredArgsConstructor
public class OAuthGoogleService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Build the Google OAuth authorization URL using configured redirectUri.
     */
    public String getGoogleAuthorizationUrl() {
        return getGoogleAuthorizationUrl(null, null);
    }

    /**
     * Build the Google OAuth authorization URL using optional redirectUri and state provided by the front-end.
     * - If redirectUri is null or blank, the configured redirectUri property is used.
     * - If state is provided, it will be attached to the URL.
     *
     * This method does one job: produce the authorization URL. It does not perform network calls.
     */
    public String getGoogleAuthorizationUrl(String overrideRedirectUri, String state) {
        String effectiveRedirect = (overrideRedirectUri == null || overrideRedirectUri.isBlank())
                ? this.redirectUri
                : overrideRedirectUri;

        GoogleAuthorizationCodeRequestUrl urlBuilder = new GoogleAuthorizationCodeRequestUrl(
                clientId,
                effectiveRedirect,
                Arrays.asList("email", "profile", "openid"));

        if (state != null && !state.isBlank()) {
            urlBuilder.setState(state);
        }

        return urlBuilder.build();
    }

    /**
     * Orchestrates the callback processing. Delegates heavy lifting to OAuthGoogleBuilder
     * so this service remains focused on orchestration only.
     */
    public String processGoogleCallback(String code) {
        String idTokenString = OAuthGoogleBuilder.exchangeCodeForIdToken(
                code, clientId, clientSecret, redirectUri);

        com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload =
                OAuthGoogleBuilder.verifyIdTokenAndGetPayload(idTokenString, clientId);

        User user = OAuthGoogleBuilder.findOrCreateUserFromPayload(payload, userRepository);

        AuthenticationResponseDto responseDto = OAuthGoogleBuilder.buildAuthenticationResponse(user);

        return jwtUtil.generateToken(responseDto);
    }
}