package com.uniai.chat.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ChatAiMetrics {

    public static final String CHAT_REQUEST_DURATION = "uniai.chat.request.duration";
    public static final String INTERPRETATION_DURATION = "uniai.ai.interpretation.duration";
    public static final String RESPONSE_DURATION = "uniai.ai.response.duration";
    public static final String RETRIEVAL_DURATION = "uniai.retrieval.duration";
    public static final String PROVIDER_REQUESTS = "uniai.ai.provider.requests";
    public static final String PROVIDER_FAILURES = "uniai.ai.provider.failures";
    public static final String FALLBACKS = "uniai.ai.fallbacks";
    public static final String INTERPRETATION_INVALID = "uniai.ai.interpretation.invalid";
    public static final String INTERPRETATION_OUTCOMES = "uniai.ai.interpretation.outcomes";
    public static final String INTERPRETATION_CLARIFICATIONS = "uniai.ai.interpretation.clarifications";
    public static final String INTERPRETATION_DISAGREEMENTS = "uniai.ai.interpretation.disagreements";
    public static final String RETRIEVAL_REQUESTS = "uniai.retrieval.requests";
    public static final String RETRIEVAL_EMPTY = "uniai.retrieval.empty";
    public static final String BUDGET_REJECTIONS = "uniai.ai.budget.rejections";
    public static final String ESTIMATED_TOKENS = "uniai.ai.request.estimated_tokens";
    public static final String CONTEXT_SIZE = "uniai.retrieval.context.size";
    public static final String RANKING_CANDIDATES = "uniai.retrieval.ranking.candidates";
    public static final String RANKING_SELECTED = "uniai.retrieval.ranking.selected";

    private ChatAiMetrics() {
    }

    public static void recordTimer(MeterRegistry registry, String metricName, String description, long durationNanos, String... keyValues) {
        if (registry == null || durationNanos < 0) {
            return;
        }
        Timer.builder(metricName)
                .description(description)
                .tags(tags(keyValues))
                .register(registry)
                .record(Duration.ofNanos(durationNanos));
    }

    public static void incrementCounter(MeterRegistry registry, String metricName, String description, String... keyValues) {
        if (registry == null) {
            return;
        }
        Counter.builder(metricName)
                .description(description)
                .tags(tags(keyValues))
                .register(registry)
                .increment();
    }

    public static void recordSummary(MeterRegistry registry, String metricName, String description, String baseUnit, long value, String... keyValues) {
        if (registry == null || value < 0) {
            return;
        }
        DistributionSummary.builder(metricName)
                .description(description)
                .baseUnit(baseUnit)
                .tags(tags(keyValues))
                .register(registry)
                .record(value);
    }

    public static Tags tags(String... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return Tags.empty();
        }

        List<Tag> tags = new ArrayList<>();
        for (int index = 0; index < keyValues.length; index += 2) {
            String key = keyValues[index];
            String value = index + 1 < keyValues.length ? keyValues[index + 1] : "";
            if (key == null || key.isBlank()) {
                continue;
            }
            tags.add(Tag.of(key, normalizeTagValue(value)));
        }
        return Tags.of(tags.toArray(new Tag[0]));
    }

    public static String normalizeTagValue(String value) {
        if (value == null) {
            return "unknown";
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return "unknown";
        }
        return normalized;
    }

    public static String normalizeEnumName(Enum<?> value) {
        if (value == null) {
            return "unknown";
        }
        return value.name().toLowerCase(Locale.ROOT);
    }
}
