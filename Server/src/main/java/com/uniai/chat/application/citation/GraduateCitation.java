package com.uniai.chat.application.citation;

public record GraduateCitation(
        String citationId,
        String label,
        String title,
        String url,
        String sourceType,
        Long universityId,
        String universityName,
        Long programId,
        String programName
) {
    public GraduateCitation {
        citationId = citationId == null ? "" : citationId.trim();
        label = label == null ? "" : label.trim();
        title = title == null ? "" : title.trim();
        url = url == null ? "" : url.trim();
        sourceType = sourceType == null ? "" : sourceType.trim();
        universityName = universityName == null ? "" : universityName.trim();
        programName = programName == null ? "" : programName.trim();
    }
}
