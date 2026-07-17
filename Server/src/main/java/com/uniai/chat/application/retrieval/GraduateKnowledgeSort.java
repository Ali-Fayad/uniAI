package com.uniai.chat.application.retrieval;

public record GraduateKnowledgeSort(
        GraduateKnowledgeSortField field,
        GraduateKnowledgeSortDirection direction
) {
    public GraduateKnowledgeSort {
        field = field == null ? GraduateKnowledgeSortField.NONE : field;
        direction = direction == null ? GraduateKnowledgeSortDirection.ASC : direction;
    }

    public static GraduateKnowledgeSort empty() {
        return new GraduateKnowledgeSort(GraduateKnowledgeSortField.NONE, GraduateKnowledgeSortDirection.ASC);
    }
}
