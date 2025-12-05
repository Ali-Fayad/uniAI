package com.uniai.security;

import com.uniai.dto.ResponseDto;
import com.uniai.model.User;
import com.uniai.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

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

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);

			if (jwtUtil.validateToken(token)) {
				String username = jwtUtil.getUsernameFromToken(token);

				if (username != null &&
						SecurityContextHolder.getContext().getAuthentication() == null) {

					User user = userRepository.findByUsername(username);

					if (user != null) {
						ResponseDto dto = ResponseDto.builder()
								.username(user.getUsername())
								.firstName(user.getFirstName())
								.lastName(user.getLastName())
								.email(user.getEmail())
								.isVerified(user.isVerified())
								.isTwoFacAuth(user.isTwoFacAuth())
								.build();

						UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
								dto,
								null,
								Collections.emptyList());

						authToken.setDetails(
								new WebAuthenticationDetailsSource().buildDetails(request));

						SecurityContextHolder.getContext().setAuthentication(authToken);
					}
				}
			}
		}

		filterChain.doFilter(request, response);
	}
}
