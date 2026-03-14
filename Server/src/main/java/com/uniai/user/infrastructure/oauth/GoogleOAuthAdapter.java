package com.uniai.user.infrastructure.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.uniai.shared.exception.GoogleAuthException;
import com.uniai.user.application.port.out.OAuthPort;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

/**
 * Google OAuth2 implementation of {@link OAuthPort}.
 * Handles code exchange, token verification, and user provisioning.
 * All Google library dependencies are confined here — the application layer stays clean.
 */
@Component
@RequiredArgsConstructor
public class GoogleOAuthAdapter implements OAuthPort {

    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String configuredRedirectUri;

    private final UserRepository userRepository;

    // -------------------------------------------------------------------------
    // OAuthPort
    // -------------------------------------------------------------------------

    @Override
    public String buildAuthorizationUrl(String overrideRedirectUri, String state) {
        String effectiveRedirect = (overrideRedirectUri == null || overrideRedirectUri.isBlank())
                ? configuredRedirectUri
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

    @Override
    public User findOrCreateUserFromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new GoogleAuthException("Authorization code is missing");
        }

        String idTokenString = exchangeCodeForIdToken(code);
        GoogleIdToken.Payload payload = verifyIdToken(idTokenString);
        return findOrCreateUser(payload);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String exchangeCodeForIdToken(String code) {
        GoogleTokenResponse tokenResponse;
        try {
            tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    TOKEN_ENDPOINT,
                    clientId,
                    clientSecret,
                    code,
                    configuredRedirectUri
            ).execute();
        } catch (IOException e) {
            throw new GoogleAuthException("Failed to exchange authorization code for tokens", e);
        }

        if (tokenResponse == null) {
            throw new GoogleAuthException("Empty token response from Google");
        }

        String idToken = tokenResponse.getIdToken();
        if (idToken == null || idToken.isBlank()) {
            throw new GoogleAuthException("ID token not present in token response");
        }
        return idToken;
    }

    private GoogleIdToken.Payload verifyIdToken(String idTokenString) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            throw new GoogleAuthException("Failed to verify ID token", e);
        }

        if (idToken == null) {
            throw new GoogleAuthException("Invalid ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        if (payload == null || payload.getEmail() == null || payload.getEmail().isBlank()) {
            throw new GoogleAuthException("ID token payload is invalid or missing email");
        }
        return payload;
    }

    private User findOrCreateUser(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();

        return userRepository.findByEmail(email).orElseGet(() -> {
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");

            User newUser = User.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .username(email)
                    .password(UUID.randomUUID().toString())
                    .isVerified(true)
                    .isTwoFacAuth(false)
                    .build();

            return userRepository.save(newUser);
        });
    }
}
