package com.uniai.cvbuilder.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API DTO representing a selectable CV template.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVTemplateResponse {
    private Long id;
    private String name;
    private String description;
    private String thumbnailUrl;
    private String componentName;
    private boolean isActive;
}
