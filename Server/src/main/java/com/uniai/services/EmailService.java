package com.uniai.services;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String generateVerificationCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        return sb.toString();
    }

    private String loadHtmlTemplate(String code) throws IOException {
        ClassPathResource resource =
                new ClassPathResource("templates/verification_email.html");

        String html = new String(resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);

        return html.replace("{{CODE}}", code);
    }

    public String sendVerificationCode(String userEmail)
            throws MessagingException, IOException {

        String code = generateVerificationCode();
        String htmlContent = loadHtmlTemplate(code);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(userEmail);
        helper.setSubject("uniAIVerification Code");
        helper.setText(htmlContent, true);

        mailSender.send(message);
        return code;
    }
}
