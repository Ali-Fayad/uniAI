package com.uniai.chat.application.citation;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GraduateCitationEngine {

    private static final Pattern LABEL_PATTERN = Pattern.compile("\\[(S\\d+)]");

    private GraduateCitationEngine() {
    }

    public static String buildRegistryBlock(List<GraduateCitation> citations) {
        if (citations == null || citations.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder("Sources:\n");
        int count = 0;
        for (GraduateCitation citation : citations) {
            if (citation == null || !StringUtils.hasText(citation.label()) || !StringUtils.hasText(citation.title())) {
                continue;
            }
            builder.append('[')
                    .append(citation.label())
                    .append("] ")
                    .append(citation.title())
                    .append('\n');
            count++;
        }
        if (count == 0) {
            return "";
        }
        return builder.toString().trim();
    }

    public static String appendCitationInstructions(String systemPrompt, List<GraduateCitation> citations) {
        String registry = buildRegistryBlock(citations);
        if (!StringUtils.hasText(registry)) {
            return systemPrompt;
        }

        String instructions = """
                Citation instructions:
                - cite using labels only
                - never invent URLs
                - never invent labels
                - do not modify labels
                """.trim();

        if (!StringUtils.hasText(systemPrompt)) {
            return instructions + "\n\n" + registry;
        }
        return systemPrompt + "\n\n" + instructions + "\n\n" + registry;
    }

    public static List<GraduateCitation> extractCitations(String content, List<GraduateCitation> citations) {
        if (!StringUtils.hasText(content) || citations == null || citations.isEmpty()) {
            return List.of();
        }

        Map<String, GraduateCitation> byLabel = new LinkedHashMap<>();
        for (GraduateCitation citation : citations) {
            if (citation != null && StringUtils.hasText(citation.label()) && !byLabel.containsKey(citation.label())) {
                byLabel.put(citation.label(), citation);
            }
        }

        if (byLabel.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> labels = new LinkedHashSet<>();
        Matcher matcher = LABEL_PATTERN.matcher(content);
        while (matcher.find()) {
            labels.add(matcher.group(1));
        }

        if (labels.isEmpty()) {
            return List.of();
        }

        List<GraduateCitation> resolved = new ArrayList<>();
        for (String label : labels) {
            GraduateCitation citation = byLabel.get(label);
            if (citation != null) {
                resolved.add(citation);
            }
        }
        return List.copyOf(resolved);
    }

    public static List<GraduateCitation> filterCitationsPresentInContext(List<GraduateCitation> citations, List<String> finalContext) {
        if (citations == null || citations.isEmpty() || finalContext == null || finalContext.isEmpty()) {
            return List.of();
        }

        List<String> contextEntries = finalContext.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
        if (contextEntries.isEmpty()) {
            return List.of();
        }

        String joinedContext = String.join("\n", contextEntries);
        boolean hasExplicitCitationLabels = citations.stream()
                .filter(Objects::nonNull)
                .map(GraduateCitation::label)
                .filter(StringUtils::hasText)
                .anyMatch(label -> containsExactLabel(joinedContext, label.trim()));

        List<GraduateCitation> filtered = new ArrayList<>();
        for (GraduateCitation citation : citations) {
            if (citation == null || !StringUtils.hasText(citation.label())) {
                continue;
            }

            String label = citation.label().trim();
            boolean keep = containsExactLabel(joinedContext, label);
            if (!keep && !hasExplicitCitationLabels) {
                keep = containsCitationEvidence(joinedContext, citation);
            }
            if (keep) {
                filtered.add(citation);
            }
        }
        return List.copyOf(filtered);
    }

    private static boolean containsExactLabel(String context, String label) {
        return StringUtils.hasText(context) && StringUtils.hasText(label) && context.contains("[" + label + "]");
    }

    private static boolean containsCitationEvidence(String context, GraduateCitation citation) {
        return matchesContextValue(context, citation.title())
                || matchesContextValue(context, citation.url())
                || matchesContextValue(context, citation.universityName())
                || matchesContextValue(context, citation.programName());
    }

    private static boolean matchesContextValue(String context, String value) {
        return StringUtils.hasText(context) && StringUtils.hasText(value) && context.contains(value.trim());
    }
}
