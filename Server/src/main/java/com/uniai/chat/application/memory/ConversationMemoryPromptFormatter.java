package com.uniai.chat.application.memory;

import java.util.List;
import java.util.StringJoiner;

public final class ConversationMemoryPromptFormatter {

    private ConversationMemoryPromptFormatter() {
    }

    public static String render(ConversationMemory memory) {
        if (memory == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        appendLine(builder, "schemaVersion", String.valueOf(memory.schemaVersion()));
        appendLine(builder, "lastIntent", memory.lastIntent());
        appendLine(builder, "comparisonActive", String.valueOf(memory.comparisonActive()));
        appendUniversities(builder, "activeUniversities", memory.activeUniversities());
        appendStrings(builder, "activeDegreeTypes", memory.activeDegreeTypes());
        appendUniversities(builder, "comparisonUniversities", memory.comparisonUniversities());
        appendStrings(builder, "pendingTopics", memory.pendingTopics());
        appendStrings(builder, "corrections", memory.corrections());
        appendStrings(builder, "unresolvedReferences", memory.unresolvedReferences());
        if (memory.userPreferences() != null && !memory.userPreferences().isEmpty()) {
            appendLine(builder, "preferredLanguage", memory.userPreferences().preferredLanguage());
            appendLine(builder, "affordabilityPriority", memory.userPreferences().affordabilityPriority());
            appendLine(builder, "preferredDeliveryMode", memory.userPreferences().preferredDeliveryMode());
        }
        return builder.toString().trim();
    }

    private static void appendUniversities(StringBuilder builder, String label, List<MemoryUniversityRef> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (MemoryUniversityRef ref : values) {
            if (ref == null) {
                continue;
            }
            StringBuilder entry = new StringBuilder();
            if (ref.acronym() != null) {
                entry.append(ref.acronym());
            }
            if (ref.name() != null) {
                if (entry.length() > 0) {
                    entry.append(" - ");
                }
                entry.append(ref.name());
            }
            if (ref.id() != null) {
                if (entry.length() > 0) {
                    entry.append(" (#").append(ref.id()).append(")");
                } else {
                    entry.append('#').append(ref.id());
                }
            }
            if (entry.length() > 0) {
                joiner.add(entry.toString());
            }
        }
        if (joiner.length() == 0) {
            return;
        }
        appendLine(builder, label, joiner.toString());
    }

    private static void appendStrings(StringBuilder builder, String label, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                joiner.add(value.trim());
            }
        }
        if (joiner.length() == 0) {
            return;
        }
        appendLine(builder, label, joiner.toString());
    }

    private static void appendLine(StringBuilder builder, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(label).append(": ").append(value.trim());
    }
}
