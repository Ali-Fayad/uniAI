package com.uniai.builder;

import java.time.LocalDateTime;

import org.thymeleaf.context.Context;

import com.uniai.domain.VerificationCodeType;
import com.uniai.model.VerifyCode;
import com.uniai.security.email.EmailProperties;

/**
 * Responsible for building email-related objects
 * (Thymeleaf Context & VerifyCode entity).
 */
public class EmailBuilder {

    private EmailBuilder() {
        // utility class
    }

    /* ===================== VERIFY CODE ===================== */

    public static VerifyCode buildVerifyCode(
            String email,
            String code,
            VerificationCodeType type,
            int expiryMinutes
    ) {
        return VerifyCode.builder()
                .email(email)
                .code(code)
                .type(type)
                .expirationTime(LocalDateTime.now().plusMinutes(expiryMinutes))
                .build();
    }

    /* ===================== EMAIL CONTEXT ===================== */

    public static Context buildEmailContext(
            EmailProperties.EmailMessage message,
            String code,
            EmailProperties properties
    ) {
        Context context = new Context();

        // Main content
        context.setVariable("title", message.getTitle());
        context.setVariable("paragraph", message.getParagraph());
        context.setVariable("buttonText", message.getButtonText());
        context.setVariable("buttonUrl", message.getButtonUrl());
        context.setVariable("code", code);

        // Global config
        context.setVariable("baseUrl", properties.getBaseUrl());
        context.setVariable("supportUrl", properties.getSupportUrl());

        // Footer
        EmailProperties.Footer footer = properties.getFooter();
        context.setVariable("footerSupportText", footer.getSupportText());
        context.setVariable("footerSupportLinkText", footer.getSupportLinkText());
        context.setVariable("footerCopyright", footer.getCopyright());
        context.setVariable("footerIgnoreText", footer.getIgnoreText());

        return context;
    }
}
