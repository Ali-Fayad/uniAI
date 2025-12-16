package com.uniai.security.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

    private String from;
    private int codeExpiryMinutes;
    private int codeLength;
    private String supportUrl;
    private String baseUrl;

    /** Email messages by type */
    private Map<String, EmailMessage> messages = new HashMap<>();

    /** Footer configuration */
    private Footer footer = new Footer();

    @Data
    public static class EmailMessage {
        private String subject;
        private String title;
        private String paragraph;
        private String buttonText;
        private String buttonUrl;
    }

    @Data
    public static class Footer {
        private String supportText;
        private String supportLinkText;
        private String copyright;
        private String ignoreText;
    }
}
