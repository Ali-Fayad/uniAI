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
        List<String> unsupportedConstraints,
        String resource,
        String operation,
        String city
) {
    public GraduateQueryInterpretation {
        universities = universities == null ? List.of() : List.copyOf(universities);
        degreeTypes = degreeTypes == null ? List.of() : List.copyOf(degreeTypes);
        topicKeywords = topicKeywords == null ? List.of() : List.copyOf(topicKeywords);
        unsupportedConstraints = unsupportedConstraints == null ? List.of() : List.copyOf(unsupportedConstraints);
    }

    /** Compatibility constructor for the schema used before typed routing metadata. */
    public GraduateQueryInterpretation(
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
            List<String> unsupportedConstraints,
            String resource,
            String operation
    ) {
        this(
                schemaVersion,
                intent,
                universities,
                degreeTypes,
                detailLevel,
                followUp,
                comparison,
                topicKeywords,
                ambiguous,
                clarificationNeeded,
                unsupportedConstraints,
                resource,
                operation,
                null
        );
    }

    /** Compatibility constructor for the schema used before typed routing metadata. */
    public GraduateQueryInterpretation(
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
        this(
                schemaVersion,
                intent,
                universities,
                degreeTypes,
                detailLevel,
                followUp,
                comparison,
                topicKeywords,
                ambiguous,
                clarificationNeeded,
                unsupportedConstraints,
                null,
                null,
                null
        );
    }
}
