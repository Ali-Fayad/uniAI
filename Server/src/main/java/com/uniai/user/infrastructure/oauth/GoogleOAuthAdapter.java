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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.time.Instant;

/**
 * Google OAuth2 implementation of {@link OAuthPort}.
 * Handles code exchange, token verification, and user provisioning.
 * All Google library dependencies are confined here — the application layer stays clean.
 */
@Component
public class GoogleOAuthAdapter implements OAuthPort {

    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String configuredRedirectUri;

    // -------------------------------------------------------------------------
    // OAuthPort
    // -------------------------------------------------------------------------

    @Override
    public String buildAuthorizationUrl(String overrideRedirectUri, String state) {
        String effectiveRedirect = (overrideRedirectUri == null || overrideRedirectUri.isBlank())
                ? configuredRedirectUri
                : overrideRedirectUri;
        if (!configuredRedirectUri.equals(effectiveRedirect)) {
            throw new GoogleAuthException("Google redirect URI is not allowed");
        }

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
    public GoogleProfile authenticate(String code, String redirectUri) {
        if (code == null || code.isBlank()) {
            throw new GoogleAuthException("Authorization code is missing");
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new GoogleAuthException("Redirect URI is missing");
        }
        if (!configuredRedirectUri.equals(redirectUri)) {
            throw new GoogleAuthException("Google redirect URI is not allowed");
        }

        String idTokenString = exchangeCodeForIdToken(code, redirectUri);
        GoogleIdToken.Payload payload = verifyIdToken(idTokenString);
        Boolean emailVerified = payload.getEmailVerified();
        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new GoogleAuthException("Google email is not verified");
        }
        String email = payload.getEmail() == null ? "" : payload.getEmail().trim();
        if (email.isBlank()) {
            throw new GoogleAuthException("Google profile is missing email");
        }
        return new GoogleProfile(email, true, stringClaim(payload, "given_name"), stringClaim(payload, "family_name"));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String exchangeCodeForIdToken(String code, String redirectUri) {
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

    private GoogleIdToken.Payload verifyIdToken(String idTokenString) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .setIssuer("https://accounts.google.com")
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
        if (payload == null
                || payload.getEmail() == null
                || payload.getEmail().isBlank()
                || !"https://accounts.google.com".equals(payload.getIssuer())
                || payload.getExpirationTimeSeconds() == null
                || payload.getExpirationTimeSeconds() <= Instant.now().getEpochSecond()) {
            throw new GoogleAuthException("ID token payload is invalid or missing email");
        }
        return payload;
    }

    private String stringClaim(GoogleIdToken.Payload payload, String claim) {
        Object value = payload.get(claim);
        return value == null ? "" : value.toString();
    }
}
