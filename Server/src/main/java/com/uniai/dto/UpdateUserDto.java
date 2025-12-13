package com.uniai.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partial update DTO for /users/me (PUT).
 * Fields are nullable; only non-null values will be applied.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDto {

    @Size(min = 2, max = 50)
    private String username;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    /**
     * Toggle two-factor authentication on/off.
     * If null => do not change.
     */
    private Boolean enableTwoFactor;
}
