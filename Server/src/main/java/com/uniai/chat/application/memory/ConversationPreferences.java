package com.uniai.chat.application.memory;

public record ConversationPreferences(
        String preferredLanguage,
        String affordabilityPriority,
        String preferredDeliveryMode
) {
    public ConversationPreferences {
        preferredLanguage = normalize(preferredLanguage);
        affordabilityPriority = normalize(affordabilityPriority);
        preferredDeliveryMode = normalize(preferredDeliveryMode);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public boolean isEmpty() {
        return preferredLanguage == null
                && affordabilityPriority == null
                && preferredDeliveryMode == null;
    }
}
