package com.uniai.services;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.uniai.exception.InvalidVerificationCodeException;
import com.uniai.model.User;
import com.uniai.model.VerifyCode;
import com.uniai.builder.EmailBuilder;
import com.uniai.domain.VerificationCodeType;
import com.uniai.repository.UserRepository;
import com.uniai.repository.VerifyCodeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import java.io.IOException;

//TODO : refactor while respecting single responsability
@Service
@AllArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final VerifyCodeRepository verifyCodeRepository;
    private final UserRepository userRepository;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 15;

    public String generateVerificationCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    //TODO : search for thymeleaf
    private String loadHtmlTemplate(String code, String title, String paragraph, int expiryMinutes) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/verification_email.html");
        String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        html = html.replace("{{CODE}}", code);
        html = html.replace("{{TITLE}}", title != null ? title : "Verify Your Email Address");
        html = html.replace("{{PARAGRAPH}}", paragraph != null ? paragraph
                : "Thanks for signing up for uniAI! To complete your registration, please use the verification code below.");
        html = html.replace("{{EXPIRY_MINUTES}}", String.valueOf(expiryMinutes));

        return html;
    }

    public String sendVerificationCode(String userEmail, VerificationCodeType type) {
        try {
            String code = generateVerificationCode();

            String subject = type.getSubject();
            String title = type.getTitle();
            String paragraph = type.getParagraph();

            String htmlContent = loadHtmlTemplate(code, title, paragraph, EXPIRY_MINUTES);

            verifyCodeRepository.deleteByEmailAndType(userEmail, type);

            VerifyCode newCode = EmailBuilder.getVerifyCode(userEmail, code, type, EXPIRY_MINUTES);

            verifyCodeRepository.save(newCode);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(userEmail);
            helper.setFrom("ali.nz.fayad@gmail.com"); //TODO : use conf file
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            return code;

        } catch (MessagingException | IOException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public User verifyCode(String email, String code, VerificationCodeType type) {

        VerifyCode stored = verifyCodeRepository.findTopByEmailAndTypeOrderByExpirationTimeDesc(email, type);

        if (stored == null ||
                stored.getExpirationTime().isBefore(LocalDateTime.now()) ||
                !stored.getCode().equals(code)) {

            throw new InvalidVerificationCodeException();
        }

        User user = userRepository.findByEmail(email.toLowerCase());
        if (user == null) {
            throw new InvalidVerificationCodeException();
        }

        if (type == VerificationCodeType.VERIFY) {
            user.setVerified(true);
            userRepository.save(user);
        }

        verifyCodeRepository.delete(stored);

        return user;
    }
}
