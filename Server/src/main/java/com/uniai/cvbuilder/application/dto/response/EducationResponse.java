package com.uniai.cvbuilder.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO representing an education entry linked to a CV, including university reference and dates.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EducationResponse {
    private Long id;
    private Long cvId;
    private Long universityId;
    private String degree;
    private String fieldOfStudy;
    private LocalDate startDate;
    private LocalDate endDate;
    private String grade;
    private String description;
}
