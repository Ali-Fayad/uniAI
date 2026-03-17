package com.uniai.user.domain.builder;

import com.uniai.user.domain.model.VerifyCode;
import com.uniai.user.domain.valueobject.VerificationCodeType;

import java.time.LocalDateTime;

/**
 * Domain builder for {@link VerifyCode}.
 *
 * <p>Owns the expiration-time calculation so the
 * {@code LocalDateTime.now().plusMinutes(...)} arithmetic is never duplicated
 * across application services.
 *
 * <p>The default expiry window is {@value DEFAULT_EXPIRY_MINUTES} minutes,
 * overridable via {@link #expiresInMinutes(int)}.
 */
public final class VerifyCodeBuilder {

    public static final int DEFAULT_EXPIRY_MINUTES = 15;

    private String               email;
    private String               code;
    private VerificationCodeType type;
    private int                  expiryMinutes = DEFAULT_EXPIRY_MINUTES;

    private VerifyCodeBuilder() {}

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static VerifyCodeBuilder create(String email, String code, VerificationCodeType type) {
        VerifyCodeBuilder b = new VerifyCodeBuilder();
        b.email = email;
        b.code  = code;
        b.type  = type;
        return b;
    }

    // -------------------------------------------------------------------------
    // Optional overrides
    // -------------------------------------------------------------------------

    /** Overrides the default {@value DEFAULT_EXPIRY_MINUTES}-minute expiry window. */
    public VerifyCodeBuilder expiresInMinutes(int minutes) {
        this.expiryMinutes = minutes;
        return this;
    }

    // -------------------------------------------------------------------------
    // Terminal
    // -------------------------------------------------------------------------

    public VerifyCode build() {
        return VerifyCode.builder()
                .email(email)
                .code(code)
                .type(type)
                .expirationTime(LocalDateTime.now().plusMinutes(expiryMinutes))
                .build();
    }
}
