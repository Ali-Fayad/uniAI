package com.uniai.chat.application.interpretation;

import java.util.List;

public record GraduateQueryInterpretation(
        Integer schemaVersion,
        String intent,
        List<String> universities,
        List<String> degreeTypes,
        String detailLevel,
        Boolean followUp,
        Boolean comparison,
        List<String> topicKeywords,
        Boolean ambiguous,
        String clarificationNeeded,
        List<String> unsupportedConstraints
) {
    public GraduateQueryInterpretation {
        universities = universities == null ? List.of() : List.copyOf(universities);
        degreeTypes = degreeTypes == null ? List.of() : List.copyOf(degreeTypes);
        topicKeywords = topicKeywords == null ? List.of() : List.copyOf(topicKeywords);
        unsupportedConstraints = unsupportedConstraints == null ? List.of() : List.copyOf(unsupportedConstraints);
    }
}
