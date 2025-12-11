package com.uniai.services;

import com.uniai.dto. AuthenticationResponseDto;
import com.uniai.dto.SignInDto;
import com.uniai.dto.SignUpDto;
import com.uniai.exception. InvalidEmailOrPassword;
import com.uniai.exception.InvalidTokenException;
import com.uniai.exception.VerificationNeededException;
import com.uniai.model.User;
import com.uniai.repository.UserRepository;
import com.uniai. security.JwtUtil;
import com.uniai.exception.AlreadyExistsException;
import com.uniai.builder. AuthenticationResponseBuilder;

import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String signUp(SignUpDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail().toLowerCase())) {
            throw new AlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByUsername(userDto.getUsername().toLowerCase())) {
            throw new AlreadyExistsException("Username already exists");
        }

        User user = AuthenticationResponseBuilder. getUserFromSignUpDto(userDto, passwordEncoder);

        userRepository.save(user);

        if (user.isVerified() == false) {
            emailService. sendVerificationCode(user.getEmail());
            throw new VerificationNeededException("a verification code was send, check your email!");
        }

        // Convert User to DTO and generate token with full user data
        AuthenticationResponseDto responseDto = AuthenticationResponseBuilder.getAuthenticationResponseDtoFromUser(user);
        return jwtUtil.generateToken(responseDto);
    }

    public String signIn(SignInDto userDto) {
        User user = userRepository.findByEmail(userDto. getEmail().toLowerCase());

        if (user == null || !passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        if (user.isVerified() == false) {
            emailService.sendVerificationCode(user.getEmail());
            throw new VerificationNeededException("a verification code was send, check your email!");
        }

        // Convert User to DTO and generate token with full user data
        AuthenticationResponseDto responseDto = AuthenticationResponseBuilder.getAuthenticationResponseDtoFromUser(user);
        return jwtUtil.generateToken(responseDto);
    }

    public String verifyAndGenerateToken(String email, String code) {
        User user = emailService.verifyCode(email, code);

        // Convert User to DTO and generate token with full user data
        AuthenticationResponseDto responseDto = AuthenticationResponseBuilder.getAuthenticationResponseDtoFromUser(user);
        return jwtUtil.generateToken(responseDto);
    }

    public List<AuthenticationResponseDto> getAllUsers() {
        List<User> users = userRepository. findAll();

        return users. stream()
                .map(AuthenticationResponseBuilder::getAuthenticationResponseDtoFromUser)
                .toList();
    }

    public AuthenticationResponseDto getResponseDtoByToken(String token) {
        // Now we can get the user info directly from the token!
        return jwtUtil.getUserDtoFromToken(token);
    }
}
