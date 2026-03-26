package com.uniai.user.application.service;

import com.uniai.shared.exception.*;
import com.uniai.shared.infrastructure.email.EmailUtil;
import com.uniai.shared.infrastructure.jwt.JwtTokenPayload;
import com.uniai.shared.infrastructure.jwt.JwtUtil;
import com.uniai.user.application.dto.command.*;
import com.uniai.user.application.dto.response.AuthResponseDto;
import com.uniai.user.application.port.in.*;
import com.uniai.user.application.port.out.NotificationPort;
import com.uniai.user.application.port.out.OAuthPort;
import com.uniai.user.domain.builder.UserBuilder;
import com.uniai.user.domain.builder.VerifyCodeBuilder;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.model.VerifyCode;
import com.uniai.user.domain.repository.UserRepository;
import com.uniai.user.domain.repository.VerifyCodeRepository;
import com.uniai.user.domain.valueobject.VerificationCodeType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Application service for all authentication use cases.
 * Orchestrates domain objects and outbound ports; contains no infrastructure code.
 */
@Service
@RequiredArgsConstructor
public class AuthApplicationService implements
        SignUpUseCase,
        SignInUseCase,
        VerifyEmailUseCase,
        VerifyTwoFactorUseCase,
        ForgotPasswordUseCase,
        ConfirmPasswordResetUseCase,
    GetGoogleAuthUrlUseCase,
    CheckEmailAvailabilityUseCase,
    CheckUsernameAvailabilityUseCase {

    private final UserRepository userRepository;
    private final VerifyCodeRepository verifyCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NotificationPort notificationPort;
    private final OAuthPort oAuthPort;

    @Value("${app.email.code-length:6}")
    private int codeLength;

    @Value("${app.email.code-expiry-minutes:15}")
    private int codeExpiryMinutes;

    // -------------------------------------------------------------------------
    // SignUpUseCase
    // -------------------------------------------------------------------------

    @Override
    public String signUp(SignUpCommand command) {
        if (userRepository.existsByEmail(command.getEmail().toLowerCase())) {
            throw new AlreadyExistsException("Email already registered");
        }
        if (userRepository.existsByUsername(command.getUsername().toLowerCase())) {
            throw new AlreadyExistsException("Username already exists");
        }

        User user = UserBuilder.forSignUp(
                        command.getFirstName(),
                        command.getLastName(),
                        command.getUsername(),
                        command.getEmail(),
                        passwordEncoder.encode(command.getPassword()))
                .build();

        userRepository.save(user);

        if (!user.isVerified()) {
            sendVerificationCode(user, VerificationCodeType.REGISTRATION);
            throw new VerificationNeededException("A verification code was sent — check your email!");
        }

        return jwtUtil.generateToken(toPayload(user));
    }

    // -------------------------------------------------------------------------
    // SignInUseCase
    // -------------------------------------------------------------------------

    @Override
    public String signIn(SignInCommand command) {
        User user = userRepository.findByEmail(command.getEmail().toLowerCase())
                .orElseThrow(InvalidEmailOrPassword::new);

        if (!passwordEncoder.matches(command.getPassword(), user.getPassword())) {
            throw new InvalidEmailOrPassword();
        }

        if (!user.isVerified()) {
            sendVerificationCode(user, VerificationCodeType.REGISTRATION);
            throw new VerificationNeededException("A verification code was sent — check your email!");
        }

        if (user.isTwoFacAuth()) {
            sendVerificationCode(user, VerificationCodeType.TWO_FA);
            throw new UnauthorizedAccessException("Two-factor authentication code sent to email");
        }

        return jwtUtil.generateToken(toPayload(user));
    }

    // -------------------------------------------------------------------------
    // VerifyEmailUseCase
    // -------------------------------------------------------------------------

    @Override
    public String verifyEmail(VerifyCommand command) {
        User user = verifyCodeAndGetUser(
                command.getEmail().toLowerCase(),
                command.getVerificationCode(),
            VerificationCodeType.REGISTRATION);
        return jwtUtil.generateToken(toPayload(user));
    }

    // -------------------------------------------------------------------------
    // VerifyTwoFactorUseCase
    // -------------------------------------------------------------------------

    @Override
    public String verifyTwoFactor(VerifyCommand command) {
        User user = verifyCodeAndGetUser(
                command.getEmail().toLowerCase(),
                command.getVerificationCode(),
            VerificationCodeType.TWO_FA);
        return jwtUtil.generateToken(toPayload(user));
    }

    // -------------------------------------------------------------------------
    // ForgotPasswordUseCase
    // -------------------------------------------------------------------------

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(InvalidEmailOrPassword::new);
        sendVerificationCode(user, VerificationCodeType.PASSWORD_RESET);
    }

    // -------------------------------------------------------------------------
    // ConfirmPasswordResetUseCase
    // -------------------------------------------------------------------------

    @Override
    public String confirmPasswordReset(RequestPasswordCommand command) {
        User user = verifyCodeAndGetUser(
                command.getEmail().toLowerCase(),
                command.getVerificationCode(),
            VerificationCodeType.PASSWORD_RESET);

        user.setPassword(passwordEncoder.encode(command.getNewPassword()));
        userRepository.save(user);

        return jwtUtil.generateToken(toPayload(user));
    }

    // -------------------------------------------------------------------------
    // GetGoogleAuthUrlUseCase
    // -------------------------------------------------------------------------

    @Override
    public String getGoogleAuthUrl(String overrideRedirectUri, String state) {
        return oAuthPort.buildAuthorizationUrl(overrideRedirectUri, state);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email.toLowerCase());
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username.toLowerCase());
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    /**
     * Generates an OTP code, persists it, and dispatches the notification email.
     */
    private void sendVerificationCode(User user, VerificationCodeType type) {
        String code = EmailUtil.generateVerificationCode(codeLength);

        verifyCodeRepository.deleteByUserIdAndType(user.getId(), type);

        VerifyCode verifyCode = VerifyCodeBuilder.create(user.getId(), code, type)
                .expiresInMinutes(codeExpiryMinutes)
                .build();

        verifyCodeRepository.save(verifyCode);
        notificationPort.sendVerificationEmail(user.getEmail(), type, code);
    }

    /**
     * Validates an OTP code and returns the associated user.
        * Also marks the user as verified when the type is REGISTRATION.
     */
    private User verifyCodeAndGetUser(String email, String code, VerificationCodeType type) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(InvalidVerificationCodeException::new);

        VerifyCode stored = verifyCodeRepository.findTopByUserIdAndType(user.getId(), type)
            .filter(v -> !v.isUsed())
            .orElseThrow(InvalidVerificationCodeException::new);

        if (EmailUtil.isExpired(stored.getExpiresAt()) || !stored.getCode().equals(code)) {
            throw new InvalidVerificationCodeException();
        }

        if (type == VerificationCodeType.REGISTRATION) {
            user.setVerified(true);
            userRepository.save(user);
        }

        stored.setUsed(true);
        verifyCodeRepository.save(stored);
        return user;
    }

    private JwtTokenPayload toPayload(User user) {
        return JwtTokenPayload.builder()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .isVerified(user.isVerified())
                .isTwoFacAuth(user.isTwoFacAuth())
                .build();
    }
}
