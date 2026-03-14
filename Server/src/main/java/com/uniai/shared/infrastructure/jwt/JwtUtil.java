package com.uniai.shared.infrastructure.jwt;

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
    private String secretKeyBase64;

    private SecretKey secretKey;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    @PostConstruct
    public void init() {
        if (secretKeyBase64 != null && !secretKeyBase64.isEmpty()) {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKeyBase64);
            secretKey = Keys.hmacShaKeyFor(keyBytes);
        } else {
            secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        return JwtTokenHelper.buildToken(username, now, expiryDate, secretKey);
    }

    public String generateToken(JwtTokenPayload payload) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        return JwtTokenHelper.buildToken(payload, now, expiryDate, secretKey);
    }

    public boolean validateToken(String token) {
        try {
            JwtTokenHelper.validateTokenStructure(token, secretKey);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return JwtTokenHelper.extractUsername(token, secretKey);
    }

    public JwtTokenPayload getPayloadFromToken(String token) {
        Claims claims = JwtTokenHelper.parseTokenClaims(token, secretKey);
        return JwtTokenHelper.payloadFromClaims(claims);
    }
}
