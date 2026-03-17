package com.uniai.user.application.port.in;

public interface GetGoogleAuthUrlUseCase {
    /** Builds the Google OAuth authorization URL with optional override parameters. */
    String getGoogleAuthUrl(String overrideRedirectUri, String state);
}
