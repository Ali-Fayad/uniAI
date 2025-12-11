package com.uniai.builder;

import com.uniai. dto.AuthenticationResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken. Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtSecurityBuilder {

    public static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(
            HttpServletRequest request,
            AuthenticationResponseDto authenticatedUser) {

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                authenticatedUser,
                null,
                Collections. emptyList()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authToken;
    }

    public static Map<String, Object> buildTokenClaims(AuthenticationResponseDto userDto) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDto.getUsername());
        claims.put("firstName", userDto.getFirstName());
        claims.put("lastName", userDto.getLastName());
        claims.put("email", userDto.getEmail());
        claims.put("isVerified", userDto.isVerified());
        claims.put("isTwoFacAuth", userDto.isTwoFacAuth());
        return claims;
    }

    public static AuthenticationResponseDto buildUserDtoFromClaims(Claims claims) {
        AuthenticationResponseDto dto = new AuthenticationResponseDto();
        dto.setUsername(claims. get("username", String.class));
        dto.setFirstName(claims.get("firstName", String.class));
        dto.setLastName(claims. get("lastName", String.class));
        dto.setEmail(claims.get("email", String.class));
        dto.setVerified(claims.get("isVerified", Boolean.class));
        dto.setTwoFacAuth(claims.get("isTwoFacAuth", Boolean.class));
        return dto;
    }

    // JWT Token Building
    public static String buildToken(String username, Date issuedAt, Date expiration, SecretKey secretKey) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public static String buildToken(AuthenticationResponseDto userDto, Date issuedAt, Date expiration, SecretKey secretKey) {
        return Jwts.builder()
                .setClaims(buildTokenClaims(userDto))
                .setSubject(userDto.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // JWT Token Parsing
    public static void validateTokenStructure(String token, SecretKey secretKey) {
        Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }

    public static String extractUsername(String token, SecretKey secretKey) {
        Claims claims = parseTokenClaims(token, secretKey);
        return claims.getSubject();
    }

    public static Claims parseTokenClaims(String token, SecretKey secretKey) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
