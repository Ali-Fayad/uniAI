package com.uniai.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // If you want a fixed secret key, replace this with a Base64 string of >=32 bytes
    private String SECRET_KEY_BASE64 = null;

    private SecretKey secretKey;

    // Token expiration in milliseconds (default 1 day)
    private final long EXPIRATION_MS = 86400000;

    @PostConstruct
    public void init() {
        if (SECRET_KEY_BASE64 != null && !SECRET_KEY_BASE64.isEmpty()) {
            // Decode Base64 secret
            byte[] keyBytes = java.util.Base64.getDecoder().decode(SECRET_KEY_BASE64);
            secretKey = Keys.hmacShaKeyFor(keyBytes);
        } else {
            // Generate a secure random 256-bit key for HS256
            secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            // Optional: save the Base64 string to log or env
            System.out.println("Generated JWT secret (Base64): " + java.util.Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        }
    }

    // Generate JWT token
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // Parse and validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Get username (subject) from token
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
