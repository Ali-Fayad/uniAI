package com.uniai.cvbuilder.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregated view model returned by CV endpoints, combining core CV metadata with all nested
 * sections and personal info.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CVResponse {
    private Long id;
    private Long userId;
    private String cvName;
    private String template;
    private boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PersonalInfoResponse personalInfo;
    private List<EducationResponse> education;
    private List<ExperienceResponse> experience;
    private List<SkillResponse> skills;
    private List<ProjectResponse> projects;
    private List<LanguageResponse> languages;
    private List<CertificateResponse> certificates;
}
