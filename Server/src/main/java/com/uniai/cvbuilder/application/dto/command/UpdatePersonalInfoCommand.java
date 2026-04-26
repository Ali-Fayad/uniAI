package com.uniai.cvbuilder.application.dto.command;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Command payload for upserting personal contact and profile information tied to the authenticated user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonalInfoCommand {

    @Pattern(regexp = "^\\+\\d+ \\d+$", message = "Phone must be in format '+{countryCode} {number}' (e.g. +1 234567890)")
    private String phone;
    private String address;
    private String linkedin;
    private String github;
    private String portfolio;
    private String summary;
    private String jobTitle;
    private String company;

    private List<EducationEntryCommand> education;
    private List<SkillEntryCommand> skills;
    private List<LanguageEntryCommand> languages;
    private List<ExperienceEntryCommand> experience;
    private List<ProjectEntryCommand> projects;
    private List<CertificateEntryCommand> certificates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationEntryCommand {
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillEntryCommand {
        private String id;
        private String skillId;
        private String name;
        private String level;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanguageEntryCommand {
        private String id;
        private String languageId;
        private String name;
        private String proficiency;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceEntryCommand {
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectEntryCommand {
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateEntryCommand {
        private String id;
        private String name;
        private String issuer;
        private String date;
        private String credentialUrl;
    }
}
