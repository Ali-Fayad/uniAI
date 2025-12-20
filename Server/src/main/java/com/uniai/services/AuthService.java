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
import com.uniai.utils.ValidationUtils;
import com.uniai.utils.HashUtils;
import com.uniai.exception.InputValidationException;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String signUp(SignUpDto userDto) {

        // Server-side validation (re-validate everything per validation_rules.MD)
        String email = ValidationUtils.toLower(userDto.getEmail());
        String username = ValidationUtils.trim(userDto.getUsername());
        String firstName = ValidationUtils.trim(userDto.getFirstName());
        String lastName = ValidationUtils.trim(userDto.getLastName());
        String frontendHash = ValidationUtils.trim(userDto.getPassword());

        if (!ValidationUtils.isValidEmail(email)) {
            throw new InputValidationException("Invalid email format");
        }

        if (!ValidationUtils.isValidUsername(username)) {
            throw new InputValidationException(
                    "Invalid username. Must be at least 2 characters and contain only letters, numbers, or underscore");
        }

        if (!ValidationUtils.isAlphaName(firstName) || !ValidationUtils.isAlphaName(lastName)) {
            throw new InputValidationException(
                    "First name and last name must contain only alphabetic characters and be at least 2 characters long");
        }

        // Ensure frontend hashed password looks like a SHA-256 hex
        if (!ValidationUtils.isValidFrontendPasswordHash(frontendHash)) {
            throw new InputValidationException("Password must be SHA-256 hashed on the client before submission");
        }

        // Normalize for uniqueness checks
        if (userRepository.existsByEmail(email)) {
            throw new AlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByUsername(username.toLowerCase())) {
            throw new AlreadyExistsException("Username already exists");
        }

        // Per rules: backend must hash the received SHA-256 value again before storing
        String serverSideHash = HashUtils.sha256Hex(frontendHash);
        userDto.setPassword(serverSideHash);

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
        // Re-validate incoming credentials per validation_rules.MD
        String email = ValidationUtils.toLower(userDto.getEmail());
        String frontendHash = ValidationUtils.trim(userDto.getPassword());

        if (!ValidationUtils.isValidEmail(email) || !ValidationUtils.isValidFrontendPasswordHash(frontendHash)) {
            throw new InvalidEmailOrPassword();
        }

        User user = userRepository.findByEmail(email);

        // Compute server-side hash of the frontend-provided SHA-256 value before
        // matching
        String serverSideHash = HashUtils.sha256Hex(frontendHash);

        if (user == null || !passwordEncoder.matches(serverSideHash, user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        if (user.isVerified() == false) {
            emailService.sendVerificationCode(user.getEmail(), VerificationCodeType.VERIFY);
            throw new VerificationNeededException("a verification code was send, check your email!");
        }

        if (user.isTwoFacAuth() == true) {
            // automatically request the TWO_FACT_AUTH code and tell caller that
            // verification is needed
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
        // normalize email to lower-case for verification lookups (your repositories
        // expect lower-case)
        User user = emailService.verifyCode(email.toLowerCase(), code, type);

        // Convert User to DTO and generate token with full user data
        AuthenticationResponseDto responseDto = AuthenticationResponseBuilder
                .getAuthenticationResponseDtoFromUser(user);
        return jwtUtil.generateToken(responseDto);
    }

    /**
     * Convenience method specifically for two-factor verification check + token
     * generation.
     * Call this after signIn() has triggered a TWO_FACT_AUTH email to be sent.
     *
     * Example flow:
     * 1) client calls /auth/signin -> signIn() throws VerificationNeededException
     * and server sends TWO_FACT_AUTH
     * 2) client shows "enter 2fa code" UI and calls /auth/2fa/verify (or similar)
     * with email + code
     * 3) controller calls this method to verify the TWO_FACT_AUTH code and return
     * the JWT
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

        // Expect frontend to send SHA-256 hashed values; compute server-side hash
        String currentFrontendHash = ValidationUtils.trim(Password);
        String newFrontendHash = ValidationUtils.trim(newPassword);

        if (!ValidationUtils.isValidFrontendPasswordHash(currentFrontendHash)
                || !ValidationUtils.isValidFrontendPasswordHash(newFrontendHash)) {
            throw new InvalidEmailOrPassword();
        }

        String currentServerHash = HashUtils.sha256Hex(currentFrontendHash);
        if (user == null || !passwordEncoder.matches(currentServerHash, user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        String newServerHash = HashUtils.sha256Hex(newFrontendHash);
        user.setPassword(passwordEncoder.encode(newServerHash));
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
     * Verify the CHANGE_PASSWORD code, update the user's password and return a new
     * JWT.
     */
    public String resetPasswordWithCode(String email, String code, String newPassword) {
        User user = emailService.verifyCode(email, code, VerificationCodeType.CHANGE_PASSWORD);

        // set the new password
        // Expect frontend provided SHA-256 of the new password; hash again on server
        // before storing
        String newServerHash = HashUtils.sha256Hex(ValidationUtils.trim(newPassword));
        user.setPassword(passwordEncoder.encode(newServerHash));
        userRepository.save(user);

        // return a new JWT for the user
        AuthenticationResponseDto responseDto = AuthenticationResponseBuilder
                .getAuthenticationResponseDtoFromUser(user);
        return jwtUtil.generateToken(responseDto);
    }

    // removed requestTwoFactorCode() as you said you won't call it manually
}
