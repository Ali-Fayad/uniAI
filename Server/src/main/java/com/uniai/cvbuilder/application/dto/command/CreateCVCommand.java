package com.uniai.cvbuilder.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command payload for creating a new CV shell with optional template selection and default flag.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCVCommand {

    @NotBlank
    private String cvName;

    private String template;

    private Boolean isDefault;
}
