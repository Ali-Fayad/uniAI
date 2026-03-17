package com.uniai.shared.infrastructure.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Carries the claims embedded in a JWT token.
 * Used exclusively by the JWT infrastructure layer (JwtUtil, JwtFilter).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtTokenPayload {
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    @Builder.Default
    private boolean isVerified = false;

    @Builder.Default
    private boolean isTwoFacAuth = false;
}
