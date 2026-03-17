package com.uniai.user.application.port.out;

import com.uniai.user.domain.model.User;

/**
 * Outbound port — implemented in infrastructure.
 * Encapsulates all Google OAuth interactions so the application layer
 * has no direct dependency on Google client libraries.
 */
public interface OAuthPort {
    /**
     * Builds the Google OAuth authorization URL.
     *
     * @param overrideRedirectUri optional redirect URI; uses the configured default when null/blank
     * @param state               optional state parameter passed through the OAuth flow
     * @return the fully-formed authorization URL
     */
    String buildAuthorizationUrl(String overrideRedirectUri, String state);

    /**
     * Exchanges the authorization code for an ID token, verifies it,
     * and finds or creates the corresponding user.
     *
     * @param code the authorization code received from Google
     * @return the domain User (existing or newly created)
     */
    User findOrCreateUserFromCode(String code);
}
