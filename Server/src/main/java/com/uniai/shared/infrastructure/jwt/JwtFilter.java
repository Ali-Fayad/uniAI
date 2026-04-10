package com.uniai.shared.infrastructure.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return uri != null && (uri.equals("/api/auth") || uri.startsWith("/api/auth/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication required");
            return;
        }

        JwtTokenPayload payload = jwtUtil.getPayloadFromToken(token);

        if (payload.getUsername() == null || payload.getUsername().isBlank()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication required");
            return;
        }

        if (!payload.isVerified()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication required");
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authToken =
                JwtTokenHelper.buildSpringAuthToken(request, payload);

        // Store the email (lowercase) as principal for easy retrieval via JwtFacade
        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(
                payload.getEmail().toLowerCase(),
                authToken.getCredentials(),
                authToken.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(principal);
        filterChain.doFilter(request, response);
    }
}
