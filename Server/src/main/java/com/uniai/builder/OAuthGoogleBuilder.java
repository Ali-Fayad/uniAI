package com.uniai.builder;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.exception.GoogleAuthException;
import com.uniai.model.User;
import com.uniai.repository.UserRepository;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * Static helper responsible for all Google OAuth-related building, verification and user creation.
 * Keeps OAuthGoogleService clean and single-responsibility.
 *
 * This class isolates external library checked exceptions and converts them into a project-level
 * unchecked GoogleAuthException so callers don't need to declare throws or handle checked exceptions.
 */
public final class OAuthGoogleBuilder {

    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    private OAuthGoogleBuilder() {
        // utility
    }

    /**
     * Exchange authorization code for a Google ID token string.
     * Converts checked IO exceptions into GoogleAuthException.
     */
    public static String exchangeCodeForIdToken(String code,
                                                String clientId,
                                                String clientSecret,
                                                String redirectUri) {
        if (code == null || code.isBlank()) {
            throw new GoogleAuthException("Authorization code is missing");
        }

        GoogleTokenResponse tokenResponse;
        try {
            tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    TOKEN_ENDPOINT,
                    clientId,
                    clientSecret,
                    code,
                    redirectUri
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

    /**
     * Verify the ID token and return its payload. Throws GoogleAuthException on any failure.
     */
    public static GoogleIdToken.Payload verifyIdTokenAndGetPayload(String idTokenString, String clientId) {
        if (idTokenString == null || idTokenString.isBlank()) {
            throw new GoogleAuthException("ID token string is empty");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            // verifier.verify can throw checked exceptions depending on the environment; wrap them.
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

    /**
     * Find an existing user by email or create a new user from the token payload.
     * The method persists new users to the provided UserRepository.
     */
    public static User findOrCreateUserFromPayload(GoogleIdToken.Payload payload, UserRepository userRepository) {
        if (payload == null) {
            throw new GoogleAuthException("Payload cannot be null");
        }
        String email = payload.getEmail();
        if (email == null || email.isBlank()) {
            throw new GoogleAuthException("Email is missing in payload");
        }

        User user = userRepository.findByEmail(email);
        if (user != null) {
            return user;
        }

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

        userRepository.save(newUser);
        return newUser;
    }

    /**
     * Build AuthenticationResponseDto from User by delegating to the existing AuthenticationResponseBuilder.
     * Keeps construction logic centralized here.
     */
    public static AuthenticationResponseDto buildAuthenticationResponse(User user) {
        if (user == null) {
            throw new GoogleAuthException("User cannot be null when building authentication response");
        }
        AuthenticationResponseDto dto = AuthenticationResponseBuilder.getAuthenticationResponseDtoFromUser(user);
        if (dto == null) {
            throw new GoogleAuthException("Failed to build AuthenticationResponseDto from User");
        }
        return dto;
    }
}