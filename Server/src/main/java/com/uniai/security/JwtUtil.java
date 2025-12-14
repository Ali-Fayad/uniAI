package com.uniai.security;

import com.uniai.builder.JwtSecurityBuilder;
import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.exception.InvalidEmailOrPassword;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:}")
    private String SECRET_KEY_BASE64;

    private SecretKey secretKey;

    @Value("${jwt.expiration-ms:86400000}")
    private long EXPIRATION_MS;

    @PostConstruct
    public void init() {
        if (SECRET_KEY_BASE64 != null && !SECRET_KEY_BASE64.isEmpty()) {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(SECRET_KEY_BASE64);
            secretKey = Keys.hmacShaKeyFor(keyBytes);
        } else {
            secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);

        return JwtSecurityBuilder.buildToken(username, now, expiryDate, secretKey);
    }

    public String generateToken(AuthenticationResponseDto userDto) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);

        return JwtSecurityBuilder.buildToken(userDto, now, expiryDate, secretKey);
    }

    /**
     * Extract raw token string from an HTTP Authorization header.
     * Throws InvalidEmailOrPassword (existing app exception) for missing/invalid header.
     */
    public String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidEmailOrPassword(); // reuse application exception for unauthorized-like case
        }
        return authHeader.substring(7);
    }

    /**
     * Convenience method for controllers: given the full Authorization header,
     * extract token, validate it and return the authenticated user's email (lowercased).
     *
     * Throws InvalidEmailOrPassword when header is missing/invalid or token invalid.
     */
    public String extractEmailFromAuthorizationHeader(String authHeader) {
        String token = extractToken(authHeader);
        try {
            JwtSecurityBuilder.validateTokenStructure(token, secretKey);
            AuthenticationResponseDto dto = getUserDtoFromToken(token);
            if (dto == null || dto.getEmail() == null || dto.getEmail().isBlank()) {
                throw new InvalidEmailOrPassword();
            }
            return dto.getEmail().toLowerCase();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidEmailOrPassword();
        }
    }

    /**
     * Validate token structure (signature/format/claims) without throwing library-specific exceptions.
     * Returns true when valid, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            JwtSecurityBuilder.validateTokenStructure(token, secretKey);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return JwtSecurityBuilder.extractUsername(token, secretKey);
    }

    public AuthenticationResponseDto getUserDtoFromToken(String token) {
        Claims claims = JwtSecurityBuilder.parseTokenClaims(token, secretKey);
        return JwtSecurityBuilder.buildUserDtoFromClaims(claims);
    }
}