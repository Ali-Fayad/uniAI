package com.uniai.shared.infrastructure.email;

import org.junit.jupiter.api.Test;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.core.env.StandardEnvironment;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailConfigurationDiagnosticsTest {

    @Test
    void maskEmailShouldHideTheLocalPart() {
        assertEquals("a***@gmail.com", EmailConfigurationDiagnostics.maskEmail("ali.nz.fayad@gmail.com"));
        assertEquals("unknown", EmailConfigurationDiagnostics.maskEmail(null));
        assertEquals("unknown", EmailConfigurationDiagnostics.maskEmail("   "));
    }

    @Test
    void recipientDomainShouldReturnOnlyTheDomain() {
        assertEquals("gmail.com", EmailConfigurationDiagnostics.recipientDomain("ali.nz.fayad@gmail.com"));
        assertEquals("unknown", EmailConfigurationDiagnostics.recipientDomain(null));
    }

    @Test
    void passwordDiagnosticsShouldCaptureLengthWhitespaceAndQuotesWithoutLeakingValue() {
        EmailConfigurationDiagnostics.PasswordDiagnostics diagnostics =
                EmailConfigurationDiagnostics.diagnosePassword("  abcd\" efgh  ");

        assertTrue(diagnostics.present());
        assertEquals(14, diagnostics.length());
        assertTrue(diagnostics.containsWhitespace());
        assertTrue(diagnostics.containsQuotes());

        String summary = EmailConfigurationDiagnostics.buildStartupSummary(mailProperties(), emailProperties(), new StandardEnvironment());
        assertFalse(summary.contains("raawfnankjxziifs"));
        assertFalse(summary.contains("ali.nz.fayad@gmail.com"));
        assertFalse(summary.contains("noreply@uniai.com"));
        assertTrue(summary.contains("passwordPresent=true"));
        assertTrue(summary.contains("passwordLength=16"));
    }

    @Test
    void sendAndFailureSummariesShouldStaySanitized() {
        String sendSummary = EmailConfigurationDiagnostics.buildSendStartedSummary(
                "REGISTRATION",
                "verify",
                "alice@example.com",
                mailProperties(),
                emailProperties(),
                "verification_email"
        );

        assertTrue(sendSummary.contains("recipientDomain=example.com"));
        assertTrue(sendSummary.contains("username=a***@gmail.com"));
        assertTrue(sendSummary.contains("from=n***@uniai.com"));
        assertFalse(sendSummary.contains("alice@example.com"));
        assertFalse(sendSummary.contains("raawfnankjxziifs"));

        String failureSummary = EmailConfigurationDiagnostics.buildAuthenticationFailureSummary(
                mailProperties(),
                emailProperties(),
                814L,
                new jakarta.mail.AuthenticationFailedException("535-5.7.8 Username and Password not accepted")
        );

        assertTrue(failureSummary.contains("host=smtp.gmail.com"));
        assertTrue(failureSummary.contains("passwordPresent=true"));
        assertTrue(failureSummary.contains("passwordContainsWhitespace=false"));
        assertTrue(failureSummary.contains("passwordContainsQuotes=false"));
        assertTrue(failureSummary.contains("fromMatchesUsername=false"));
        assertTrue(failureSummary.contains("durationMs=814"));
        assertFalse(failureSummary.contains("raawfnankjxziifs"));
        assertFalse(failureSummary.contains("ali.nz.fayad@gmail.com"));
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

    private static EmailProperties emailProperties() {
        EmailProperties properties = new EmailProperties();
        properties.setFrom("noreply@uniai.com");
        properties.setFooter(new EmailProperties.Footer());
        properties.getMessages().put("verify", new EmailProperties.EmailMessage());
        properties.getMessages().put("two-factor", new EmailProperties.EmailMessage());
        properties.getMessages().put("change-password", new EmailProperties.EmailMessage());
        return properties;
    }
}
