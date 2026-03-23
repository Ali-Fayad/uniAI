package com.uniai.cvbuilder.application.dto.response;

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
    private String phone;
    private String address;
    private String linkedin;
    private String github;
    private String portfolio;
    private String summary;
    private String jobTitle;
    private String company;
}
