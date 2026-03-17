package com.uniai.user.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    @Builder.Default
    private boolean isVerified = false;

    @Builder.Default
    private boolean isTwoFacAuth = false;
}
