package com.uniai.admin.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailsResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private boolean isVerified;
    private boolean isTwoFacAuth;
    private long chatCount;
    private long messageCount;
    private double averageMessagesPerChat;
    private long cvCount;
}
