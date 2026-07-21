package com.uniai.user.application.service;

import com.uniai.shared.exception.InvalidUsernameException;

import java.util.Locale;

/** Central username normalization used by signup and profile operations. */
public final class UsernameNormalizer {

    private UsernameNormalizer() {
    }

    public static String normalize(String username) {
        if (username == null) {
            throw new InvalidUsernameException("Username is required");
        }
        String normalized = username.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() < 2 || normalized.length() > 50
                || !normalized.matches("^[a-z0-9_]+$")) {
            throw new InvalidUsernameException("Username must contain 2–50 letters, numbers, or underscores");
        }
        return normalized;
    }
}
