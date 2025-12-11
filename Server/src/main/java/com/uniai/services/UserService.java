package com.uniai.services;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.uniai.builder.AuthenticationResponseBuilder;
import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.exception.InvalidEmailOrPassword;
import com.uniai.model.User;
import com.uniai.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

	private final PasswordEncoder passwordEncoder;
	final UserRepository userRepository;

	public List<AuthenticationResponseDto> getAllUsers() {
		List<User> users = userRepository.findAll();

		return users.stream()
				.map(AuthenticationResponseBuilder::getAuthenticationResponseDtoFromUser)
				.toList();
	}

	public void deleteUserByEmail(String email) {
		boolean deleted = userRepository.deleteByEmail(email);
		if (!deleted) {
			throw new RuntimeException("User not found with email: " + email);
		}
	}

	public void changePasswordWithoutOTP(String email, String Password, String newPassword) {
		User user = userRepository.findByUsername(email);

		if (user == null || !passwordEncoder.matches(Password, user.getPassword())) {
			throw new InvalidEmailOrPassword();
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

	}
}
