package com.uniai.chat.application.retrieval;

public record GraduateKnowledgeAggregation(
        GraduateKnowledgeAggregationFunction function,
        String field
) {
    public GraduateKnowledgeAggregation {
        function = function == null ? GraduateKnowledgeAggregationFunction.NONE : function;
        field = field == null || field.isBlank() ? null : field.trim();
    }

    public static GraduateKnowledgeAggregation empty() {
        return new GraduateKnowledgeAggregation(GraduateKnowledgeAggregationFunction.NONE, null);
    }
}
