package com.uniai.cvbuilder.application.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing user-level contact and profile details merged into CV responses.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalInfoResponse {
    private Long userId;
    private boolean hasPersonalInfo;
    private boolean isFilled;
    private String phone;
    private String address;
    private String linkedin;
    private String github;
    private String portfolio;
    private String summary;
    private String jobTitle;
    private String company;
    private List<EducationEntryResponse> education;
    private List<SkillEntryResponse> skills;
    private List<LanguageEntryResponse> languages;
    private List<ExperienceEntryResponse> experience;
    private List<ProjectEntryResponse> projects;
    private List<CertificateEntryResponse> certificates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationEntryResponse {
        private String id;
        private Long universityId;
        private String universityName;
        private String degree;
        private String fieldOfStudy;
        private String startDate;
        private String endDate;
        private String grade;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillEntryResponse {
        private String id;
        private String skillId;
        private String name;
        private String level;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanguageEntryResponse {
        private String id;
        private String languageId;
        private String name;
        private String proficiency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceEntryResponse {
        private String id;
        private String positionId;
        private String position;
        private String company;
        private String location;
        private String startDate;
        private String endDate;
        private Boolean currentlyWorking;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectEntryResponse {
        private String id;
        private String name;
        private String description;
        private String repositoryUrl;
        private String liveUrl;
        private String startDate;
        private String endDate;
        private List<String> technologies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateEntryResponse {
        private String id;
        private String name;
        private String issuer;
        private String date;
        private String credentialUrl;
    }
}
