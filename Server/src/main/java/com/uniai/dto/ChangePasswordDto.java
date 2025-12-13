package com.uniai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Used by POST /api/users/change-password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDto {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, message = "password must be at least 8 characters")
    private String newPassword;
}
