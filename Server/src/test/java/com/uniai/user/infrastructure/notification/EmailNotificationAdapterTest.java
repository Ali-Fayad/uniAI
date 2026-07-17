package com.uniai.user.infrastructure.notification;

import com.uniai.shared.infrastructure.email.EmailProperties;
import com.uniai.user.domain.valueobject.VerificationCodeType;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailNotificationAdapterTest {

    @Test
    void sendVerificationEmailShouldRethrowMailAuthenticationException() {
        RecordingMailSender mailSender = new RecordingMailSender(true);
        TemplateEngine templateEngine = templateEngine();

        EmailNotificationAdapter adapter = new EmailNotificationAdapter(
                mailSender,
                templateEngine,
                emailProperties(),
                mailProperties()
        );

        MailAuthenticationException exception = assertThrows(
                MailAuthenticationException.class,
                () -> adapter.sendVerificationEmail("alice@example.com", VerificationCodeType.REGISTRATION, "ABC123")
        );

        assertSame(mailSender.thrownException, exception);
        assertEquals("SMTP authentication failed", exception.getMessage());
    }

    @Test
    void sendVerificationEmailShouldCreateAndSendMessageWhenAuthSucceeds() {
        RecordingMailSender mailSender = new RecordingMailSender(false);
        TemplateEngine templateEngine = templateEngine();

        EmailNotificationAdapter adapter = new EmailNotificationAdapter(
                mailSender,
                templateEngine,
                emailProperties(),
                mailProperties()
        );

        adapter.sendVerificationEmail("alice@example.com", VerificationCodeType.REGISTRATION, "ABC123");

        assertEquals("alice@example.com", mailSender.lastRecipient);
        assertEquals("uniAI — Email Verification", mailSender.lastSubject);
        assertEquals("noreply@uniai.com", mailSender.lastFrom);
    }

    private static TemplateEngine templateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.clearDialects();
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setCacheable(false);
        resolver.setTemplateMode(TemplateMode.HTML);
        templateEngine.setTemplateResolver(resolver);
        return templateEngine;
    }

    private static EmailProperties emailProperties() {
        EmailProperties properties = new EmailProperties();
        properties.setFrom("noreply@uniai.com");
        properties.setBaseUrl("http://localhost:9090");
        properties.setSupportUrl("mailto:support@uniai.com");
        properties.setCodeExpiryMinutes(15);
        EmailProperties.Footer footer = new EmailProperties.Footer();
        footer.setSupportText("support");
        footer.setSupportLinkText("contact");
        footer.setCopyright("copyright");
        footer.setIgnoreText("ignore");
        properties.setFooter(footer);

        EmailProperties.EmailMessage message = new EmailProperties.EmailMessage();
        message.setSubject("uniAI — Email Verification");
        message.setTitle("Verify Your Email Address");
        message.setParagraph("Thanks for signing up");
        message.setButtonText("Verify Your Account");
        message.setButtonUrl("/api/auth/verify");
        Map<String, EmailProperties.EmailMessage> messages = new LinkedHashMap<>();
        messages.put("verify", message);
        properties.setMessages(messages);
        return properties;
    }

    private static MailProperties mailProperties() {
        MailProperties properties = new MailProperties();
        properties.setHost("smtp.gmail.com");
        properties.setPort(587);
        properties.setUsername("ali.nz.fayad@gmail.com");
        properties.setPassword("raawfnankjxziifs");
        properties.getProperties().put("mail.smtp.auth", "true");
        properties.getProperties().put("mail.smtp.starttls.enable", "true");
        properties.getProperties().put("mail.smtp.starttls.required", "true");
        return properties;
    }

    private static final class RecordingMailSender extends JavaMailSenderImpl {
        private final boolean failAuthentication;
        private String lastRecipient;
        private String lastSubject;
        private String lastFrom;
        private MailAuthenticationException thrownException;

        private RecordingMailSender(boolean failAuthentication) {
            this.failAuthentication = failAuthentication;
        }

        @Override
        public void send(MimeMessage... mimeMessages) {
            if (failAuthentication) {
                thrownException = new MailAuthenticationException(
                        "SMTP authentication failed",
                        new AuthenticationFailedException("535-5.7.8 Username and Password not accepted")
                );
                throw thrownException;
            }
            if (mimeMessages != null && mimeMessages.length > 0) {
                try {
                    lastRecipient = mimeMessages[0].getAllRecipients()[0].toString();
                    lastSubject = mimeMessages[0].getSubject();
                    lastFrom = mimeMessages[0].getFrom()[0].toString();
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        }
    }
}
