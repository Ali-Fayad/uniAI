package com.uniai.shared.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Low-level JWT token builder and parser.
 * Works exclusively with {@link JwtTokenPayload} — no dependency on any domain or application class.
 */
public final class JwtTokenHelper {

    private static final String DEFAULT_ROLE = "USER";

    private JwtTokenHelper() {
        // utility
    }

    public static UsernamePasswordAuthenticationToken buildSpringAuthToken(
            HttpServletRequest request,
            JwtTokenPayload payload) {

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                payload,
                null,
                buildAuthorities(payload.getRole())
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authToken;
    }

    public static List<SimpleGrantedAuthority> buildAuthorities(String role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + normalizeRole(role)));
    }

    public static Map<String, Object> buildClaims(JwtTokenPayload payload) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", payload.getUsername());
        claims.put("firstName", payload.getFirstName());
        claims.put("lastName", payload.getLastName());
        claims.put("email", payload.getEmail());
        claims.put("role", normalizeRole(payload.getRole()));
        claims.put("isVerified", payload.isVerified());
        claims.put("isTwoFacAuth", payload.isTwoFacAuth());
        return claims;
    }

    public static JwtTokenPayload payloadFromClaims(Claims claims) {
        String role = normalizeRole(claims.get("role", String.class));
        return JwtTokenPayload.builder()
                .username(claims.get("username", String.class))
                .firstName(claims.get("firstName", String.class))
                .lastName(claims.get("lastName", String.class))
                .email(claims.get("email", String.class))
                .role(role)
                .isVerified(Boolean.TRUE.equals(claims.get("isVerified", Boolean.class)))
                .isTwoFacAuth(Boolean.TRUE.equals(claims.get("isTwoFacAuth", Boolean.class)))
                .build();
    }

    public static String buildToken(String subject, Date issuedAt, Date expiration, SecretKey secretKey) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public static String buildToken(JwtTokenPayload payload, Date issuedAt, Date expiration, SecretKey secretKey) {
        return Jwts.builder()
                .setClaims(buildClaims(payload))
                .setSubject(payload.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public static void validateTokenStructure(String token, SecretKey secretKey) {
        Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }

    public static String extractUsername(String token, SecretKey secretKey) {
        return parseTokenClaims(token, secretKey).getSubject();
    }

    public static Claims parseTokenClaims(String token, SecretKey secretKey) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private static String normalizeRole(String role) {
        if ("ADMIN".equals(role)) {
            return "ADMIN";
        }
        return DEFAULT_ROLE;
    }
}
