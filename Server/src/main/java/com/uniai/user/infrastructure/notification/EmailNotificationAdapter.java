package com.uniai.user.infrastructure.notification;

import com.uniai.shared.infrastructure.email.EmailProperties;
import com.uniai.user.application.port.out.NotificationPort;
import com.uniai.user.domain.valueobject.VerificationCodeType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Email-based implementation of {@link NotificationPort}.
 * Translates application-level notification requests into SMTP messages via Thymeleaf + JavaMail.
 */
@Component
@RequiredArgsConstructor
public class EmailNotificationAdapter implements NotificationPort {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailProperties emailProperties;

    @Override
    public void sendVerificationEmail(String toEmail, VerificationCodeType type, String code) {
        EmailProperties.EmailMessage message = getEmailMessage(type);
        Context context = buildContext(code, message);
        sendEmail(toEmail, message.getSubject(), context);
    }

    private EmailProperties.EmailMessage getEmailMessage(VerificationCodeType type) {
        String key = typeToKey(type);
        EmailProperties.EmailMessage message = emailProperties.getMessages().get(key);
        if (message == null) {
            throw new IllegalStateException("Missing email configuration for type: " + key);
        }
        return message;
    }

    private Context buildContext(String code, EmailProperties.EmailMessage message) {
        Context context = new Context();
        context.setVariable("title", message.getTitle());
        context.setVariable("paragraph", message.getParagraph());
        context.setVariable("buttonText", message.getButtonText());
        context.setVariable("buttonUrl", message.getButtonUrl());
        context.setVariable("code", code);
        context.setVariable("baseUrl", emailProperties.getBaseUrl());
        context.setVariable("supportUrl", emailProperties.getSupportUrl());

        EmailProperties.Footer footer = emailProperties.getFooter();
        context.setVariable("footerSupportText", footer.getSupportText());
        context.setVariable("footerSupportLinkText", footer.getSupportLinkText());
        context.setVariable("footerCopyright", footer.getCopyright());
        context.setVariable("footerIgnoreText", footer.getIgnoreText());

        return context;
    }

    private void sendEmail(String to, String subject, Context context) {
        try {
            String html = templateEngine.process("verification_email", context);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom(emailProperties.getFrom());
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Email sending failed", e);
        }
    }

    private String typeToKey(VerificationCodeType type) {
        return switch (type) {
            case REGISTRATION, EMAIL_CHANGE -> "verify";
            case TWO_FA -> "two-factor";
            case PASSWORD_RESET -> "change-password";
        };
    }
}
