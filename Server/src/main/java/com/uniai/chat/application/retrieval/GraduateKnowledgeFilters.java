package com.uniai.chat.application.retrieval;

import java.util.List;

/**
 * Immutable filter boundary for graduate knowledge queries.
 *
 * Only currently supported filters are populated. Future retrieval tasks can
 * add typed dimensions here without introducing an unbounded map contract.
 */
public record GraduateKnowledgeFilters(
        List<ResolvedUniversity> universities,
        List<String> degreeTypes,
        List<String> topicKeywords
) {
    public GraduateKnowledgeFilters {
        universities = universities == null ? List.of() : List.copyOf(universities);
        degreeTypes = degreeTypes == null ? List.of() : List.copyOf(degreeTypes);
        topicKeywords = topicKeywords == null ? List.of() : List.copyOf(topicKeywords);
    }

    public static GraduateKnowledgeFilters empty() {
        return new GraduateKnowledgeFilters(List.of(), List.of(), List.of());
    }
}
