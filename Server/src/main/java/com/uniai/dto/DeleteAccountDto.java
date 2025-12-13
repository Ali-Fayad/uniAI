package com.uniai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for DELETE /api/users/me
 * For safety we require the user's current password to perform deletion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountDto {
    @NotBlank
    private String password;
}
