package com.uniai.cvbuilder.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<ExperienceEntryResponse> experience;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationEntryResponse {
        private String id;
        private Long universityId;
        private String universityName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillEntryResponse {
        private String id;
        private String skillId;
        private String name;
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
    }
}
