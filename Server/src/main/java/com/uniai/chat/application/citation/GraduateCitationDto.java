package com.uniai.chat.application.citation;

public record GraduateCitationDto(
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
    public static GraduateCitationDto from(GraduateCitation citation) {
        if (citation == null) {
            return null;
        }
        return new GraduateCitationDto(
                citation.citationId(),
                citation.label(),
                citation.title(),
                citation.url(),
                citation.sourceType(),
                citation.universityId(),
                citation.universityName(),
                citation.programId(),
                citation.programName()
        );
    }
}
