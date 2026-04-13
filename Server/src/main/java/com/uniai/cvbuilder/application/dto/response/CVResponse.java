package com.uniai.cvbuilder.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Long templateId;
    private String templateName;
    private String templateComponentName;
    private String template;
    private List<String> sectionsOrder;
    private com.uniai.cvbuilder.application.dto.SelectedItemsDto selectedItems;
    private com.uniai.cvbuilder.application.dto.ItemsOrderDto itemsOrder;
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
