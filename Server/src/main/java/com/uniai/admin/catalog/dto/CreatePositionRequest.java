package com.uniai.admin.catalog.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePositionRequest(
        @NotBlank
        @Size(max = 255)
        @Pattern(regexp = "^[^\\p{Cc}<>]*$", message = "Name contains unsupported characters")
        String name) {
}
