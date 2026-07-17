package com.uniai.shared.infrastructure.email;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class EmailConfigurationDiagnostics {

    private static final Logger logger = LogManager.getLogger(EmailConfigurationDiagnostics.class);

    private final MailProperties mailProperties;
    private final EmailProperties emailProperties;
    private final Environment environment;

    public EmailConfigurationDiagnostics(MailProperties mailProperties, EmailProperties emailProperties, Environment environment) {
        this.mailProperties = mailProperties;
        this.emailProperties = emailProperties;
        this.environment = environment;
    }

    @PostConstruct
    public void logStartupConfiguration() {
        logger.info("[EMAIL_CONFIG] {}", buildStartupSummary(mailProperties, emailProperties, environment));
    }

    public static String buildStartupSummary(MailProperties mailProperties, EmailProperties emailProperties, Environment environment) {
        PasswordDiagnostics passwordDiagnostics = diagnosePassword(mailProperties.getPassword());
        Map<String, String> properties = mailProperties.getProperties();
        String username = mailProperties.getUsername();
        String from = emailProperties.getFrom();

        return "smtpHost=" + valueOrUnknown(mailProperties.getHost())
                + " smtpPort=" + mailProperties.getPort()
                + " authEnabled=" + booleanProperty(properties, "mail.smtp.auth")
                + " starttlsEnabled=" + booleanProperty(properties, "mail.smtp.starttls.enable")
                + " starttlsRequired=" + booleanProperty(properties, "mail.smtp.starttls.required")
                + " username=" + maskEmail(username)
                + " usernameLength=" + length(username)
                + " passwordPresent=" + passwordDiagnostics.present()
                + " passwordLength=" + passwordDiagnostics.length()
                + " passwordContainsWhitespace=" + passwordDiagnostics.containsWhitespace()
                + " passwordContainsQuotes=" + passwordDiagnostics.containsQuotes()
                + " from=" + maskEmail(from)
                + " fromMatchesUsername=" + equalsIgnoreCaseRaw(from, username)
                + " messageKeys=" + loadedMessageKeys(emailProperties)
                + " footerConfigured=" + (emailProperties.getFooter() != null)
                + " activeProfiles=" + activeProfiles(environment);
    }

    public static String buildSendStartedSummary(String emailType,
                                                String messageKey,
                                                String recipientEmail,
                                                MailProperties mailProperties,
                                                EmailProperties emailProperties,
                                                String templateName) {
        PasswordDiagnostics passwordDiagnostics = diagnosePassword(mailProperties.getPassword());
        return "type=" + safeValue(emailType)
                + " key=" + safeValue(messageKey)
                + " recipientDomain=" + recipientDomain(recipientEmail)
                + " smtpHost=" + valueOrUnknown(mailProperties.getHost())
                + " smtpPort=" + mailProperties.getPort()
                + " username=" + maskEmail(mailProperties.getUsername())
                + " from=" + maskEmail(emailProperties.getFrom())
                + " passwordPresent=" + passwordDiagnostics.present()
                + " passwordLength=" + passwordDiagnostics.length()
                + " template=" + safeValue(templateName);
    }

    public static String buildAuthenticationFailureSummary(MailProperties mailProperties,
                                                           EmailProperties emailProperties,
                                                           long durationMs,
                                                           Throwable cause) {
        PasswordDiagnostics passwordDiagnostics = diagnosePassword(mailProperties.getPassword());
        return "host=" + valueOrUnknown(mailProperties.getHost())
                + " port=" + mailProperties.getPort()
                + " username=" + maskEmail(mailProperties.getUsername())
                + " passwordPresent=" + passwordDiagnostics.present()
                + " passwordLength=" + passwordDiagnostics.length()
                + " passwordContainsWhitespace=" + passwordDiagnostics.containsWhitespace()
                + " passwordContainsQuotes=" + passwordDiagnostics.containsQuotes()
                + " fromMatchesUsername=" + equalsIgnoreCaseRaw(emailProperties.getFrom(), mailProperties.getUsername())
                + " durationMs=" + durationMs
                + " cause=" + (cause == null ? "null" : cause.getClass().getSimpleName())
                + " smtpMessage=" + safeSingleLine(cause == null ? null : cause.getMessage());
    }

    static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "unknown";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return email.charAt(0) + "***";
        }
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        return localPart.charAt(0) + "***@" + domain;
    }

    static String recipientDomain(String email) {
        if (email == null || email.isBlank()) {
            return "unknown";
        }
        int atIndex = email.indexOf('@');
        if (atIndex < 0 || atIndex == email.length() - 1) {
            return "unknown";
        }
        return email.substring(atIndex + 1);
    }

    static PasswordDiagnostics diagnosePassword(String password) {
        boolean present = password != null && !password.isEmpty();
        int length = password == null ? 0 : password.length();
        boolean containsWhitespace = password != null && password.chars().anyMatch(Character::isWhitespace);
        boolean containsQuotes = password != null && (password.contains("\"") || password.contains("'"));
        return new PasswordDiagnostics(present, length, containsWhitespace, containsQuotes);
    }

    static boolean equalsIgnoreCaseRaw(String first, String second) {
        if (first == null || second == null) {
            return false;
        }
        return first.equalsIgnoreCase(second);
    }

    static String safeSingleLine(String value) {
        if (value == null) {
            return "null";
        }
        return value.replace("\r", " ").replace("\n", " ").trim();
    }

    private static String valueOrUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    private static boolean booleanProperty(Map<String, String> properties, String key) {
        String value = properties == null ? null : properties.get(key);
        return value != null && Boolean.parseBoolean(value);
    }

    private static String loadedMessageKeys(EmailProperties emailProperties) {
        if (emailProperties.getMessages() == null || emailProperties.getMessages().isEmpty()) {
            return "[]";
        }
        return emailProperties.getMessages().keySet().stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String activeProfiles(Environment environment) {
        if (environment == null) {
            return "[]";
        }
        String[] profiles = environment.getActiveProfiles();
        if (profiles == null || profiles.length == 0) {
            return "[]";
        }
        List<String> active = Arrays.stream(profiles)
                .filter(Objects::nonNull)
                .sorted()
                .toList();
        return active.stream().collect(Collectors.joining(", ", "[", "]"));
    }

    private static String safeValue(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    static int length(String value) {
        return value == null ? 0 : value.length();
    }

    public record PasswordDiagnostics(boolean present, int length, boolean containsWhitespace, boolean containsQuotes) {
    }
}
