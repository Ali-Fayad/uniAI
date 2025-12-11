package com.uniai.security;

import com.uniai.builder.JwtSecurityBuilder;
import com.uniai.dto.AuthenticationResponseDto;
import io.jsonwebtoken. Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private String SECRET_KEY_BASE64 = null;

    private SecretKey secretKey;

    private final long EXPIRATION_MS = 86400000;

    @PostConstruct
    public void init() {
        if (SECRET_KEY_BASE64 != null && !SECRET_KEY_BASE64.isEmpty()) {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(SECRET_KEY_BASE64);
            secretKey = Keys. hmacShaKeyFor(keyBytes);
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
        Date expiryDate = new Date(now. getTime() + EXPIRATION_MS);

        return JwtSecurityBuilder.buildToken(userDto, now, expiryDate, secretKey);
    }

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
        return JwtSecurityBuilder. buildUserDtoFromClaims(claims);
    }
}
