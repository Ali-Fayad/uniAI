package com.uniai.services;

import com.uniai.builder.AuthenticationResponseBuilder;
import com.uniai.dto.AuthenticationResponseDto;
import com.uniai.dto.SignInDto;
import com.uniai.dto.SignUpDto;
import com.uniai.exception.AlreadyExistsException;
import com.uniai.exception.InvalidEmailOrPassword;
import com.uniai.exception.VerificationNeededException;
import com.uniai.model.User;
import com.uniai.domain.VerificationCodeType;
import com.uniai.repository.UserRepository;
import com.uniai.security.JwtUtil;
import lombok.AllArgsConstructor;
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

        User user = AuthenticationResponseBuilder.getUserFromSignUpDto(userDto, passwordEncoder);

        userRepository.save(user);

        if (user.isVerified() == false) {
            emailService.sendVerificationCode(user.getEmail(), VerificationCodeType.VERIFY);
            throw new VerificationNeededException("a verification code was send, check your email!");
        }

        AuthenticationResponseDto responseDto = AuthenticationResponseBuilder
                .getAuthenticationResponseDtoFromUser(user);
        return jwtUtil.generateToken(responseDto);
    }

    public String signIn(SignInDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail().toLowerCase());

        if (user == null || !passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        if (user.isVerified() == false) {
            emailService.sendVerificationCode(user.getEmail(), VerificationCodeType.VERIFY);
            throw new VerificationNeededException("a verification code was send, check your email!");
        }

        // Convert User to DTO and generate token with full user data
        AuthenticationResponseDto responseDto = AuthenticationResponseBuilder
                .getAuthenticationResponseDtoFromUser(user);
        return jwtUtil.generateToken(responseDto);
    }

    public String verifyAndGenerateToken(String email, String code) {
        User user = emailService.verifyCode(email, code, VerificationCodeType.VERIFY);

        // Convert User to DTO and generate token with full user data
        AuthenticationResponseDto responseDto = AuthenticationResponseBuilder
                .getAuthenticationResponseDtoFromUser(user);
        return jwtUtil.generateToken(responseDto);
    }

    public AuthenticationResponseDto getResponseDtoByToken(String token) {
        // Now we can get the user info directly from the token!
        return jwtUtil.getUserDtoFromToken(token);
    }

    public void changePasswordWithOTP(String email, String Password, String newPassword) {
        User user = userRepository.findByUsername(email);

        if (user == null || !passwordEncoder.matches(Password, user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Send a CHANGE_PASSWORD verification code to the provided email.
     */
    public void forgetPassword(String email) {
        User user = userRepository.findByEmail(email.toLowerCase());
        if (user == null) {
            // For security you might want to return 200 even if email not found.
            throw new InvalidEmailOrPassword();
        }
        emailService.sendVerificationCode(email, VerificationCodeType.CHANGE_PASSWORD);
    }

    /**
     * Verify the CHANGE_PASSWORD code, update the user's password and return a new JWT.
     */
    public String resetPasswordWithCode(String email, String code, String newPassword) {
        User user = emailService.verifyCode(email, code, VerificationCodeType.CHANGE_PASSWORD);

        // set the new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // return a new JWT for the user
        AuthenticationResponseDto responseDto = AuthenticationResponseBuilder
                .getAuthenticationResponseDtoFromUser(user);
        return jwtUtil.generateToken(responseDto);
    }

    /**
     * Convenience: request a two-factor code (TWO_FACT_AUTH).
     */
    public String requestTwoFactorCode(String email) {
        return emailService.sendVerificationCode(email, VerificationCodeType.TWO_FACT_AUTH);
    }
}
