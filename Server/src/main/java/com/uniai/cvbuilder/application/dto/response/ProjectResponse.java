package com.uniai.cvbuilder.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing a project section entry persisted for a CV, including repo and live links.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    private Long id;
    private Long cvId;
    private String name;
    private String description;
    private String githubUrl;
    private String liveUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> technologies;
}
