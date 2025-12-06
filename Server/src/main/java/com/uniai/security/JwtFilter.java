package com.uniai.security;

import com.uniai.builder.AuthenticationResponseBuilder;
import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.model.User;
import com.uniai.repository.UserRepository;

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
	private final UserRepository userRepository;

	public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
		this.jwtUtil = jwtUtil;
		this.userRepository = userRepository;
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

		return AuthenticationResponseBuilder.getUsernamePasswordAuthenticationToken(request, authenticatedUser);

	}



	private AuthenticationResponseDto extractUser( String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer "))
			return null;

		String token = authHeader.substring(7);

		if (!jwtUtil.validateToken(token))
			throw new RuntimeException("Token is invalid");

		String username = jwtUtil.getUsernameFromToken(token);

		if (username == null || SecurityContextHolder.getContext().getAuthentication() != null)
			throw new RuntimeException("Can't extract username from token");

		User user = userRepository.findByUsername(username);

		if (user == null)
			throw new RuntimeException("User not found");

		if (user.isVerified())
			throw new RuntimeException("User not verified");

		AuthenticationResponseDto dto = AuthenticationResponseBuilder.getAuthenticationResponseDtoFromUser(user);
		return dto;

	}



}
