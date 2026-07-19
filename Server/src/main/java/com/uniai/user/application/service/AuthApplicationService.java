package com.uniai.user.application.service;

import com.uniai.shared.exception.*;
import com.uniai.shared.infrastructure.email.EmailUtil;
import com.uniai.shared.infrastructure.jwt.JwtTokenPayload;
import com.uniai.shared.infrastructure.jwt.JwtUtil;
import com.uniai.user.application.dto.command.*;
import com.uniai.user.application.dto.response.AuthResponseDto;
import com.uniai.user.application.dto.response.SignUpResultDto;
import com.uniai.user.application.port.in.*;
import com.uniai.user.application.port.out.NotificationPort;
import com.uniai.user.application.port.out.OAuthPort;
import com.uniai.user.domain.builder.UserBuilder;
import com.uniai.user.domain.builder.VerifyCodeBuilder;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.model.VerifyCode;
import com.uniai.user.domain.repository.UserRepository;
import com.uniai.user.domain.repository.VerifyCodeRepository;
import com.uniai.user.domain.valueobject.UserRole;
import com.uniai.user.domain.valueobject.VerificationCodeType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.text.Normalizer;
import java.util.Base64;
import java.util.Locale;
import java.security.SecureRandom;

/**
 * Application service for all authentication use cases.
 * Orchestrates domain objects and outbound ports; contains no infrastructure code.
 */
@Service
@RequiredArgsConstructor
public class AuthApplicationService implements
        SignUpUseCase,
        ResendVerificationCodeUseCase,
        SignInUseCase,
        VerifyEmailUseCase,
        VerifyTwoFactorUseCase,
        ForgotPasswordUseCase,
        ConfirmPasswordResetUseCase,
    GetGoogleAuthUrlUseCase,
    CompleteGoogleLoginUseCase,
    CheckEmailAvailabilityUseCase,
    CheckUsernameAvailabilityUseCase {

    private final UserRepository userRepository;
    private final VerifyCodeRepository verifyCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NotificationPort notificationPort;
    private final OAuthPort oAuthPort;

    private static final String VERIFICATION_REQUIRED_MESSAGE = "A verification code was sent — check your email!";
    private static final String VERIFICATION_RESEND_SUCCESS_MESSAGE = "If verification is needed, a new code will be sent shortly.";
    private static final String VERIFICATION_RESEND_RATE_LIMIT_MESSAGE = "Please wait before requesting another verification code.";

    @Value("${app.email.code-length:6}")
    private int codeLength;

    @Value("${app.email.code-expiry-minutes:15}")
    private int codeExpiryMinutes;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri:http://localhost:5173/google/callback}")
    private String googleRedirectUri;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int MAX_USERNAME_ATTEMPTS = 10_000;

    // -------------------------------------------------------------------------
    // SignUpUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public SignUpResultDto signUp(SignUpCommand command) {
        if (userRepository.existsByEmail(command.getEmail().toLowerCase())) {
            throw new AlreadyExistsException("Email already registered");
        }
        if (userRepository.existsByUsername(command.getUsername().toLowerCase())) {
            throw new AlreadyExistsException("Username already exists");
        }

        boolean isFirstUser = userRepository.count() == 0;
        UserRole role = isFirstUser ? UserRole.ADMIN : UserRole.USER;
        // The first registered account bootstraps admin access; later accounts default to USER.
        // Role is decided server-side and is never accepted from the signup request.
        User user = UserBuilder.forSignUp(
                        command.getFirstName(),
                        command.getLastName(),
                        command.getUsername(),
                        command.getEmail(),
                        passwordEncoder.encode(command.getPassword()))
                .role(role)
                .build();
        userRepository.save(user);

        sendVerificationCode(user, VerificationCodeType.REGISTRATION);

        return SignUpResultDto.verificationRequired(VERIFICATION_REQUIRED_MESSAGE);
    }

    // -------------------------------------------------------------------------
    // ResendVerificationCodeUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public String resendVerificationCode(EmailRequestCommand command) {
        String email = command.getEmail().toLowerCase();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || user.isVerified()) {
            return VERIFICATION_RESEND_SUCCESS_MESSAGE;
        }

        VerifyCode latest = verifyCodeRepository
                .findTopByUserIdAndType(user.getId(), VerificationCodeType.REGISTRATION)
                .orElse(null);

        if (latest != null && verifyCodeRepository.existsByUserIdAndTypeAndUsedTrue(user.getId(), VerificationCodeType.REGISTRATION)
                && isCooldownActive(latest)) {
            throw new VerificationCodeRateLimitException(VERIFICATION_RESEND_RATE_LIMIT_MESSAGE);
        }

        sendVerificationCode(user, VerificationCodeType.REGISTRATION);
        return VERIFICATION_RESEND_SUCCESS_MESSAGE;
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
            throw new VerificationNeededException(VERIFICATION_REQUIRED_MESSAGE);
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
    @Transactional
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
    @Transactional
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
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(InvalidEmailOrPassword::new);
        sendVerificationCode(user, VerificationCodeType.PASSWORD_RESET);
    }

    // -------------------------------------------------------------------------
    // ConfirmPasswordResetUseCase
    // -------------------------------------------------------------------------

    @Override
    @Transactional
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
        return oAuthPort.buildAuthorizationUrl(validateGoogleRedirectUri(overrideRedirectUri), state);
    }

    @Override
    @Transactional
    public String completeGoogleLogin(String code, String redirectUri) {
        String validatedRedirectUri = validateGoogleRedirectUri(redirectUri);
        OAuthPort.GoogleProfile profile = oAuthPort.authenticate(code, validatedRedirectUri);
        if (profile == null || profile.email() == null || profile.email().isBlank()
                || !profile.emailVerified()) {
            throw new GoogleAuthException("Google profile could not be validated");
        }

        String email = normalizeEmail(profile.email());
        User user = userRepository.findByEmail(email).orElseGet(() -> createGoogleUser(profile, email));
        return jwtUtil.generateToken(toPayload(user));
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email.toLowerCase());
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username.toLowerCase());
    }

    private String validateGoogleRedirectUri(String redirectUri) {
        String candidate = redirectUri == null || redirectUri.isBlank() ? googleRedirectUri : redirectUri.trim();
        if (candidate == null || !candidate.equals(googleRedirectUri)) {
            throw new GoogleAuthException("Google redirect URI is not allowed");
        }
        return candidate;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private User createGoogleUser(OAuthPort.GoogleProfile profile, String email) {
        String firstName = safeName(profile.firstName());
        String lastName = safeName(profile.lastName());
        String encodedPassword = passwordEncoder.encode(generateRandomPassword());
        String baseUsername = googleUsernameBase(firstName, lastName, email);

        for (int suffix = 0; suffix < MAX_USERNAME_ATTEMPTS; suffix++) {
            String username = suffix == 0 ? baseUsername : baseUsername + suffix;
            if (username.length() > 50 || userRepository.existsByUsername(username)) {
                continue;
            }
            try {
                return userRepository.saveAndFlush(UserBuilder.forGoogleOAuth(
                        firstName, lastName, username, email, encodedPassword).build());
            } catch (DataIntegrityViolationException ex) {
                if (!isUsernameConstraintViolation(ex)) {
                    throw ex;
                }
            }
        }
        throw new GoogleAuthException("Unable to allocate a unique Google username");
    }

    private boolean isUsernameConstraintViolation(DataIntegrityViolationException exception) {
        String message = exception.getMostSpecificCause() == null
                ? exception.getMessage()
                : exception.getMostSpecificCause().getMessage();
        String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT);
        return normalized.contains("username") && normalized.contains("unique");
    }

    private String safeName(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }

    private String googleUsernameBase(String firstName, String lastName, String email) {
        String first = sanitizeUsername(firstName);
        String last = sanitizeUsername(lastName);
        String base;
        if (!first.isBlank() && !last.isBlank()) {
            base = first.substring(0, 1) + last;
        } else {
            String localPart = email.substring(0, Math.max(0, email.indexOf('@')));
            base = sanitizeUsername(localPart);
        }
        if (base.isBlank()) base = "googleuser";
        // Leave room for the numeric suffix while staying within User's 50-character constraint.
        return base.substring(0, Math.min(base.length(), 45));
    }

    private String sanitizeUsername(String value) {
        if (value == null || value.isBlank()) return "";
        String ascii = Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return ascii.replaceAll("[^a-z0-9]", "");
    }

    private String generateRandomPassword() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    /**
     * Generates an OTP code, persists it, and dispatches the notification email.
     */
    private void sendVerificationCode(User user, VerificationCodeType type) {
        String code = EmailUtil.generateVerificationCode(codeLength);

        verifyCodeRepository.markByUserIdAndTypeUsed(user.getId(), type);

        VerifyCode verifyCode = VerifyCodeBuilder.create(user.getId(), code, type)
                .expiresInMinutes(codeExpiryMinutes)
                .build();

        verifyCodeRepository.save(verifyCode);
        notificationPort.sendVerificationEmail(user.getEmail(), type, code);
    }

    private boolean isCooldownActive(VerifyCode latest) {
        return latest.getCreatedAt() != null
                && latest.getCreatedAt().plusMinutes(1).isAfter(java.time.LocalDateTime.now());
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
                .role(user.getRole() == null ? UserRole.USER.name() : user.getRole().name())
                .isVerified(user.isVerified())
                .isTwoFacAuth(user.isTwoFacAuth())
                .build();
    }
}
