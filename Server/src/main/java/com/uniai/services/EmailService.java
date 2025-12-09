package com. uniai.services;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java. time.LocalDateTime;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail. javamail.MimeMessageHelper;
import org. springframework.stereotype.Service;

import com.uniai.exception.InvalidVerificationCodeException;
import com.uniai.model.User;
import com.uniai.model.VerifyCode;
import com.uniai.repository.UserRepository;
import com.uniai.repository.VerifyCodeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet. MimeMessage;
import lombok.AllArgsConstructor;
import java.io.IOException;

@Service
@AllArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final VerifyCodeRepository verifyCodeRepository;
    private final UserRepository userRepository;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;

    public String generateVerificationCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private String loadHtmlTemplate(String code) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/verification_email.html");

        String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return html.replace("{{CODE}}", code);
    }

    public String sendVerificationCode(String userEmail) {
        try {
            String code = generateVerificationCode();
            String htmlContent = loadHtmlTemplate(code);

            verifyCodeRepository.deleteByEmail(userEmail);

            VerifyCode newCode = VerifyCode.builder()
                    .email(userEmail)
                    .code(code)
                    .expirationTime(LocalDateTime.now().plusMinutes(15))
                    .build();

            verifyCodeRepository.save(newCode);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(userEmail);
            helper. setSubject("uniAI Verification Code");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            return code;

        } catch (MessagingException | IOException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public User verifyCode(String email, String code) {

        VerifyCode stored = verifyCodeRepository.findTopByEmailOrderByExpirationTimeDesc(email);

        if (stored == null ||
                stored.getExpirationTime().isBefore(LocalDateTime.now()) ||
                ! stored.getCode().equals(code)) {

            throw new InvalidVerificationCodeException();
        }

        User user = userRepository.findByEmail(email. toLowerCase());
        if (user == null) {
            throw new InvalidVerificationCodeException();
        }

        user.setVerified(true);
        userRepository.save(user);

        verifyCodeRepository. delete(stored);

        return user;
    }
}
