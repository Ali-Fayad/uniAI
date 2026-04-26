package com.uniai.cvbuilder.application.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.uniai.cvbuilder.application.dto.response.CVResponse;
import com.uniai.cvbuilder.application.dto.response.CertificateResponse;
import com.uniai.cvbuilder.application.dto.response.EducationResponse;
import com.uniai.cvbuilder.application.dto.response.ExperienceResponse;
import com.uniai.cvbuilder.application.dto.response.LanguageResponse;
import com.uniai.cvbuilder.application.dto.response.PersonalInfoResponse;
import com.uniai.cvbuilder.application.dto.response.ProjectResponse;
import com.uniai.cvbuilder.application.dto.response.SkillResponse;
import com.uniai.cvbuilder.domain.model.CV;
import com.uniai.cvbuilder.domain.model.CVTemplate;
import com.uniai.cvbuilder.domain.model.Certificate;
import com.uniai.cvbuilder.domain.model.Education;
import com.uniai.cvbuilder.domain.model.Experience;
import com.uniai.cvbuilder.domain.model.Language;
import com.uniai.cvbuilder.domain.model.PersonalInfo;
import com.uniai.cvbuilder.domain.model.Project;
import com.uniai.cvbuilder.domain.model.SelectedItems;
import com.uniai.cvbuilder.domain.model.Skill;

/**
 * Mapping utilities to convert CV domain aggregates and nested entities into response DTO graphs.
 */
public final class CVMapper {

    private CVMapper() {}

    public static CVResponse toResponse(
            CV cv,
            CVTemplate template,
            PersonalInfo personalInfo,
            List<Education> educations,
            List<Experience> experiences,
            List<Skill> skills,
            List<Project> projects,
            List<Language> languages,
            List<Certificate> certificates
    ) {
        return CVResponse.builder()
                .id(cv.getId())
                .userId(cv.getUserId())
                .cvName(cv.getCvName())
                .templateId(cv.getTemplateId())
                .templateName(template != null ? template.getName() : null)
                .templateComponentName(template != null ? template.getComponentName() : cv.getTemplate())
                .template(cv.getTemplate())
                .sectionsOrder(cv.getSectionsOrder())
                .selectedItems(mapSelectedItems(cv.getSelectedItems()))
                .itemsOrder(mapItemsOrder(cv.getItemsOrder()))
                .isDefault(cv.isDefault())
                .createdAt(cv.getCreatedAt())
                .updatedAt(cv.getUpdatedAt())
                .personalInfo(mapPersonalInfo(personalInfo))
                .education(mapEducation(educations))
                .experience(mapExperience(experiences))
                .skills(mapSkills(skills))
                .projects(mapProjects(projects))
                .languages(mapLanguages(languages))
                .certificates(mapCertificates(certificates))
                .build();
    }

    public static PersonalInfoResponse mapPersonalInfo(PersonalInfo personalInfo) {
        if (personalInfo == null) {
            return null;
        }
        return PersonalInfoResponse.builder()
                .userId(personalInfo.getUserId())
                .phone(personalInfo.getPhone())
                .address(personalInfo.getAddress())
                .linkedin(personalInfo.getLinkedin())
                .github(personalInfo.getGithub())
                .portfolio(personalInfo.getPortfolio())
                .summary(personalInfo.getSummary())
                .jobTitle(personalInfo.getJobTitle())
                .company(personalInfo.getCompany())
                .build();
    }

    public static com.uniai.cvbuilder.application.dto.SelectedItemsDto mapSelectedItems(SelectedItems selectedItems) {
        if (selectedItems == null) {
            return null;
        }
        return com.uniai.cvbuilder.application.dto.SelectedItemsDto.builder()
                .skillIds(selectedItems.getSkillIds() != null ? selectedItems.getSkillIds() : List.of())
                .languageIds(selectedItems.getLanguageIds() != null ? selectedItems.getLanguageIds() : List.of())
                .educationIds(selectedItems.getEducationIds() != null ? selectedItems.getEducationIds() : List.of())
                .experienceIds(selectedItems.getExperienceIds() != null ? selectedItems.getExperienceIds() : List.of())
                .projectIds(selectedItems.getProjectIds() != null ? selectedItems.getProjectIds() : List.of())
                .certificateIds(selectedItems.getCertificateIds() != null ? selectedItems.getCertificateIds() : List.of())
                .build();
    }

    public static com.uniai.cvbuilder.application.dto.ItemsOrderDto mapItemsOrder(com.uniai.cvbuilder.domain.model.ItemsOrder itemsOrder) {
        if (itemsOrder == null) {
            return null;
        }
        return com.uniai.cvbuilder.application.dto.ItemsOrderDto.builder()
                .skillIds(itemsOrder.getSkillIds() != null ? itemsOrder.getSkillIds() : List.of())
                .languageIds(itemsOrder.getLanguageIds() != null ? itemsOrder.getLanguageIds() : List.of())
                .educationIds(itemsOrder.getEducationIds() != null ? itemsOrder.getEducationIds() : List.of())
                .experienceIds(itemsOrder.getExperienceIds() != null ? itemsOrder.getExperienceIds() : List.of())
                .projectIds(itemsOrder.getProjectIds() != null ? itemsOrder.getProjectIds() : List.of())
                .certificateIds(itemsOrder.getCertificateIds() != null ? itemsOrder.getCertificateIds() : List.of())
                .build();
    }

    public static List<EducationResponse> mapEducation(List<Education> educations) {
        return educations == null ? List.of() : educations.stream()
                .map(e -> EducationResponse.builder()
                        .id(e.getId())
                        .cvId(e.getCvId())
                        .universityId(e.getUniversityId())
                        .degree(e.getDegree())
                        .fieldOfStudy(e.getFieldOfStudy())
                        .startDate(e.getStartDate())
                        .endDate(e.getEndDate())
                        .grade(e.getGrade())
                        .description(e.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<ExperienceResponse> mapExperience(List<Experience> experiences) {
        return experiences == null ? List.of() : experiences.stream()
                .map(e -> ExperienceResponse.builder()
                        .id(e.getId())
                        .cvId(e.getCvId())
                        .position(e.getPosition())
                        .company(e.getCompany())
                        .location(e.getLocation())
                        .startDate(e.getStartDate())
                        .endDate(e.getEndDate())
                        .isCurrent(e.isCurrent())
                        .description(e.getDescription())
                        .achievements(e.getAchievements())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<SkillResponse> mapSkills(List<Skill> skills) {
        return skills == null ? List.of() : skills.stream()
                .map(s -> SkillResponse.builder()
                        .id(s.getId())
                        .cvId(s.getCvId())
                        .name(s.getName())
                        .level(s.getLevel())
                        .order(s.getOrder())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<ProjectResponse> mapProjects(List<Project> projects) {
        return projects == null ? List.of() : projects.stream()
                .map(p -> ProjectResponse.builder()
                        .id(p.getId())
                        .cvId(p.getCvId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .githubUrl(p.getGithubUrl())
                        .liveUrl(p.getLiveUrl())
                        .startDate(p.getStartDate())
                        .endDate(p.getEndDate())
                        .technologies(p.getTechnologies())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<LanguageResponse> mapLanguages(List<Language> languages) {
        return languages == null ? List.of() : languages.stream()
                .map(l -> LanguageResponse.builder()
                        .id(l.getId())
                        .cvId(l.getCvId())
                        .name(l.getName())
                        .proficiency(l.getProficiency())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<CertificateResponse> mapCertificates(List<Certificate> certificates) {
        return certificates == null ? List.of() : certificates.stream()
                .map(c -> CertificateResponse.builder()
                        .id(c.getId())
                        .cvId(c.getCvId())
                        .name(c.getName())
                        .issuer(c.getIssuer())
                        .date(c.getDate())
                        .credentialUrl(c.getCredentialUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
