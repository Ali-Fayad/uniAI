package com.uniai.cvbuilder.infrastructure.mapper;

import com.uniai.cvbuilder.application.dto.response.*;
import com.uniai.cvbuilder.domain.model.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapping utilities to convert CV domain aggregates and nested entities into response DTO graphs.
 */
public final class CVMapper {

    private CVMapper() {}

    public static CVResponse toResponse(
            CV cv,
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
                .template(cv.getTemplate())
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
