package com.uniai.user.application.port.out;


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

    /** Exchanges and validates a Google authorization code into an application-owned profile. */
    GoogleProfile authenticate(String code, String redirectUri);

    record GoogleProfile(String email, boolean emailVerified, String firstName, String lastName) {}
}
