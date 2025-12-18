package com.uniai.services;

import com.uniai.builder.AuthenticationResponseBuilder;
import com.uniai.dto.auth.AuthenticationResponseDto;
import com.uniai.dto.auth.SignInDto;
import com.uniai.dto.auth.SignUpDto;
import com.uniai.exception.AlreadyExistsException;
import com.uniai.exception.InvalidEmailOrPassword;
import com.uniai.exception.UnauthorizedAccessException;
import com.uniai.exception.VerificationNeededException;
import com.uniai.model.User;
import com.uniai.domain.VerificationCodeType;
import com.uniai.repository.UserRepository;
import com.uniai.security.jwt.JwtUtil;

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

        if (user.isTwoFacAuth() == true) {
            // automatically request the TWO_FACT_AUTH code and tell caller that verification is needed
            emailService.sendVerificationCode(user.getEmail(), VerificationCodeType.TWO_FACT_AUTH);
            throw new UnauthorizedAccessException("two-factor authentication code sent to email");
        }

        // Convert User to DTO and generate token with full user data
        AuthenticationResponseDto responseDto = AuthenticationResponseBuilder
                .getAuthenticationResponseDtoFromUser(user);
        return jwtUtil.generateToken(responseDto);
    }

    /**
     * Verify an email + code for VERIFY type and generate a token.
     * Backwards-compatible helper that defaults to VERIFY type.
     */
    public String verifyAndGenerateToken(String email, String code) {
        return verifyAndGenerateToken(email, code, VerificationCodeType.VERIFY);
    }

    /**
     * Generic verification for a specific VerificationCodeType, then generate JWT.
     */
    public String verifyAndGenerateToken(String email, String code, VerificationCodeType type) {
        // normalize email to lower-case for verification lookups (your repositories expect lower-case)
        User user = emailService.verifyCode(email.toLowerCase(), code, type);

        // Convert User to DTO and generate token with full user data
        AuthenticationResponseDto responseDto = AuthenticationResponseBuilder
                .getAuthenticationResponseDtoFromUser(user);
        return jwtUtil.generateToken(responseDto);
    }

    /**
     * Convenience method specifically for two-factor verification check + token generation.
     * Call this after signIn() has triggered a TWO_FACT_AUTH email to be sent.
     *
     * Example flow:
     * 1) client calls /auth/signin -> signIn() throws VerificationNeededException and server sends TWO_FACT_AUTH
     * 2) client shows "enter 2fa code" UI and calls /auth/2fa/verify (or similar) with email + code
     * 3) controller calls this method to verify the TWO_FACT_AUTH code and return the JWT
     */
    public String checkTwoFactorAndGenerate(String email, String code) {
        return verifyAndGenerateToken(email, code, VerificationCodeType.TWO_FACT_AUTH);
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

    // removed requestTwoFactorCode() as you said you won't call it manually
}
