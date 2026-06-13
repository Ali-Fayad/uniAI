package com.uniai.shared.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtTokenHelperTest {

    @Test
    void buildClaimsShouldIncludeRole() {
        JwtTokenPayload payload = JwtTokenPayload.builder()
                .username("alice")
                .firstName("Alice")
                .lastName("Admin")
                .email("alice@example.com")
                .role("ADMIN")
                .isVerified(true)
                .isTwoFacAuth(false)
                .build();

        Map<String, Object> claims = JwtTokenHelper.buildClaims(payload);

        assertEquals("ADMIN", claims.get("role"));
    }

    @Test
    void payloadFromClaimsShouldDefaultMissingRoleToUser() {
        Claims claims = Jwts.claims(Map.of(
                "username", "alice",
                "firstName", "Alice",
                "lastName", "Admin",
                "email", "alice@example.com",
                "isVerified", true,
                "isTwoFacAuth", false
        ));

        JwtTokenPayload payload = JwtTokenHelper.payloadFromClaims(claims);

        assertEquals("USER", payload.getRole());
    }

    @Test
    void payloadFromClaimsShouldRejectInvalidRoleToUser() {
        Claims claims = Jwts.claims(Map.of(
                "username", "alice",
                "firstName", "Alice",
                "lastName", "Admin",
                "email", "alice@example.com",
                "role", "SUPERADMIN",
                "isVerified", true,
                "isTwoFacAuth", false
        ));

        JwtTokenPayload payload = JwtTokenHelper.payloadFromClaims(claims);

        assertEquals("USER", payload.getRole());
    }
}
