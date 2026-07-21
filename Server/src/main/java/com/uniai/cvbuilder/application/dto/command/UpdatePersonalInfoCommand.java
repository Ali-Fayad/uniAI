package com.uniai.cvbuilder.application.dto.command;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 500)
    private String address;
    @Size(max = 2048)
    @Pattern(regexp = "^$|https?://\\S+$", message = "LinkedIn URL must use HTTP or HTTPS")
    private String linkedin;
    @Size(max = 2048)
    @Pattern(regexp = "^$|https?://\\S+$", message = "GitHub URL must use HTTP or HTTPS")
    private String github;
    @Size(max = 2048)
    @Pattern(regexp = "^$|https?://\\S+$", message = "Portfolio URL must use HTTP or HTTPS")
    private String portfolio;
    @Size(max = 5000)
    private String summary;
    @Size(max = 150)
    private String jobTitle;
    @Size(max = 200)
    private String company;

    @Size(max = 50)
    private List<EducationEntryCommand> education;
    @Size(max = 100)
    private List<SkillEntryCommand> skills;
    @Size(max = 50)
    private List<LanguageEntryCommand> languages;
    @Size(max = 50)
    private List<ExperienceEntryCommand> experience;
    @Size(max = 50)
    private List<ProjectEntryCommand> projects;
    @Size(max = 50)
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
