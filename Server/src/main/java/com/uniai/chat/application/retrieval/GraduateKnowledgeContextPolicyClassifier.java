package com.uniai.chat.application.retrieval;

import java.util.List;

public final class GraduateKnowledgeContextPolicyClassifier {
    private GraduateKnowledgeContextPolicyClassifier() {}

    public static GraduateKnowledgeContextPolicy classify(String message, List<ResolvedUniversity> explicitUniversities) {
        String normalized = message == null ? "" : message.trim().toLowerCase();
        boolean comparison = GraduateKnowledgeResolutionSupport.containsAny(normalized,
                "compare", "comparison", "versus", " vs ", "which one", "first", "second", "more ", "cheaper");
        if (comparison && (explicitUniversities == null || explicitUniversities.isEmpty())) {
            return GraduateKnowledgeContextPolicy.COMPARISON_CONTINUATION;
        }
        boolean referential = GraduateKnowledgeResolutionSupport.containsAny(normalized,
                " it ", "they ", "them ", "that university", "this university", "same university",
                "same", "same program", "same question", "what about", "and tuition", "how much does it", "for phd", "for master", "both");
        if (comparison) return GraduateKnowledgeContextPolicy.COMPARISON_CONTINUATION;
        if (referential) return GraduateKnowledgeContextPolicy.REFERENTIAL;
        boolean explicitScope = explicitUniversities != null && !explicitUniversities.isEmpty()
                || GraduateKnowledgeResolutionSupport.detectRequestedCity(normalized, List.of()) != null;
        return explicitScope ? GraduateKnowledgeContextPolicy.TOPIC_RESET : GraduateKnowledgeContextPolicy.STANDALONE;
    }

    public static boolean allowsInheritance(GraduateKnowledgeContextPolicy policy) {
        return policy == GraduateKnowledgeContextPolicy.REFERENTIAL
                || policy == GraduateKnowledgeContextPolicy.COMPARISON_CONTINUATION;
    }
}
