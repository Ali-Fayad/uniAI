package com.uniai.cvbuilder.application.dto.command;

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
    private List<ExperienceEntryCommand> experience;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationEntryCommand {
        private String id;
        private Long universityId;
        private String universityName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillEntryCommand {
        private String id;
        private String skillId;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceEntryCommand {
        private String id;
        private String positionId;
        private String position;
        private String company;
    }
}
