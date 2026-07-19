package com.uniai.chat.application.retrieval;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraduateKnowledgeContextPolicyClassifierTest {
    @Test
    void standaloneCityDoesNotInheritScope() {
        assertEquals(GraduateKnowledgeContextPolicy.TOPIC_RESET,
                GraduateKnowledgeContextPolicyClassifier.classify("How many universities are in Beirut?", List.of()));
    }

    @Test
    void referentialAndComparisonLanguageAllowsInheritance() {
        assertEquals(GraduateKnowledgeContextPolicy.REFERENTIAL,
                GraduateKnowledgeContextPolicyClassifier.classify("How many campuses does it have?", List.of()));
        assertEquals(GraduateKnowledgeContextPolicy.COMPARISON_CONTINUATION,
                GraduateKnowledgeContextPolicyClassifier.classify("Which one has more campuses?", List.of()));
    }
}
