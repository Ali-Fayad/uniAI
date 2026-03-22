package com.uniai.cvbuilder.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a language proficiency entry for a CV.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LanguageResponse {
    private Long id;
    private Long cvId;
    private String name;
    private String proficiency;
}
