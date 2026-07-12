package com.uniai.chat.application.citation;

import java.util.List;

public record GraduateKnowledgeRetrievalResult(
        String formattedContext,
        List<GraduateCitation> citations
) {
    public GraduateKnowledgeRetrievalResult {
        formattedContext = formattedContext == null ? "" : formattedContext;
        citations = citations == null ? List.of() : List.copyOf(citations);
    }
}
