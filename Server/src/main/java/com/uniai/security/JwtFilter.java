package com.uniai.security;

import com.uniai.builder.JwtSecurityBuilder;
import com.uniai.dto. AuthenticationResponseDto;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet. http.HttpServletRequest;
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
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain)
			throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");

		AuthenticationResponseDto authenticatedUser = extractUser(authHeader);
		UsernamePasswordAuthenticationToken authToken = getAuthTokenIfPossible(request, authenticatedUser);

		if(authToken != null)
			SecurityContextHolder.getContext().setAuthentication(authToken);

		filterChain.doFilter(request, response);
	}

	private UsernamePasswordAuthenticationToken getAuthTokenIfPossible(HttpServletRequest request, AuthenticationResponseDto authenticatedUser) {
		if(authenticatedUser == null)
			return null;

		return JwtSecurityBuilder.getUsernamePasswordAuthenticationToken(request, authenticatedUser);
	}

	private AuthenticationResponseDto extractUser(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer "))
			return null;

		String token = authHeader.substring(7);

		if (!jwtUtil.validateToken(token))
			throw new RuntimeException("Token is invalid");

		AuthenticationResponseDto dto = jwtUtil.getUserDtoFromToken(token);

		if (dto. getUsername() == null || SecurityContextHolder.getContext().getAuthentication() != null)
			throw new RuntimeException("Can't extract username from token");

		if (!dto.isVerified())
			throw new RuntimeException("User not verified");

		return dto;
	}
}
