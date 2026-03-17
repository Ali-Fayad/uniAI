package com.uniai.user.domain.builder;

import com.uniai.user.domain.model.User;

import java.util.UUID;

/**
 * Domain builder for {@link User}.
 *
 * <p>Two named factory methods cover the only two creation paths in the domain:
 * standard sign-up and OAuth provisioning. All normalization rules (lower-case,
 * capitalize, sensible defaults) live here — application services stop caring
 * about those details.
 *
 * <p><b>Password encoding is the caller's responsibility.</b> Encoding is an
 * infrastructure concern (Spring Security); the domain builder accepts an
 * already-encoded value so this class stays dependency-free.
 */
public final class UserBuilder {

    private String  firstName;
    private String  lastName;
    private String  username;
    private String  email;
    private String  encodedPassword;
    private boolean isVerified;
    private boolean isTwoFacAuth;

    private UserBuilder() {}

    // -------------------------------------------------------------------------
    // Named factory methods
    // -------------------------------------------------------------------------

    /**
     * Entry point for standard sign-up.
     * <ul>
     *   <li>email and username → lower-case</li>
     *   <li>firstName and lastName → capitalized</li>
     *   <li>isVerified = {@code false} (email verification required)</li>
     *   <li>isTwoFacAuth = {@code false}</li>
     * </ul>
     *
     * @param encodedPassword already-encoded password — never a plain-text value
     */
    public static UserBuilder forSignUp(String firstName, String lastName,
                                        String username, String email,
                                        String encodedPassword) {
        UserBuilder b = new UserBuilder();
        b.firstName       = capitalize(firstName);
        b.lastName        = capitalize(lastName);
        b.username        = username.toLowerCase();
        b.email           = email.toLowerCase();
        b.encodedPassword = encodedPassword;
        b.isVerified      = false;
        b.isTwoFacAuth    = false;
        return b;
    }

    /**
     * Entry point for OAuth-provisioned users.
     * <ul>
     *   <li>email is used as username (Google accounts are globally unique)</li>
     *   <li>isVerified = {@code true} (Google already verified the address)</li>
     *   <li>isTwoFacAuth = {@code false}</li>
     *   <li>password = random UUID (account is accessed via OAuth only)</li>
     * </ul>
     */
    public static UserBuilder forOAuth(String firstName, String lastName, String email) {
        UserBuilder b = new UserBuilder();
        b.firstName       = firstName;
        b.lastName        = lastName;
        b.username        = email;
        b.email           = email;
        b.encodedPassword = UUID.randomUUID().toString();
        b.isVerified      = true;
        b.isTwoFacAuth    = false;
        return b;
    }

    // -------------------------------------------------------------------------
    // Optional overrides
    // -------------------------------------------------------------------------

    /** Overrides the default two-factor-auth flag. */
    public UserBuilder withTwoFactorAuth(boolean enabled) {
        this.isTwoFacAuth = enabled;
        return this;
    }

    // -------------------------------------------------------------------------
    // Terminal
    // -------------------------------------------------------------------------

    public User build() {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .email(email)
                .password(encodedPassword)
                .isVerified(isVerified)
                .isTwoFacAuth(isTwoFacAuth)
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) return value;
        return Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase();
    }
}
