package com.uniai.db;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.uniai.model.User;
import com.uniai.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DevDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.existsByEmail("test@uniai.com")) {
            return;
        }

        User user = new User();
		user.setFirstName("Ali");
        user.setLastName("Fayad");
		user.setUsername("afayad123");
        user.setEmail("test@uniai.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setVerified(true);
		user.setTwoFacAuth(false);

        userRepository.save(user);

    }
}
