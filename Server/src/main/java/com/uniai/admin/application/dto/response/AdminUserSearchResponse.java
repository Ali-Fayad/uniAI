package com.uniai.admin.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSearchResponse {
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String role;
}
