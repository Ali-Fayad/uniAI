package com.uniai.cvbuilder.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a skill entry on a CV, including optional proficiency level and ordering.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkillResponse {
    private Long id;
    private Long cvId;
    private String name;
    private String level;
    private Integer order;
}
