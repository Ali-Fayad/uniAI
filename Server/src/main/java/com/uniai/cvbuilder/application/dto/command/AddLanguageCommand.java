package com.uniai.cvbuilder.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command payload for attaching a language proficiency entry to a CV.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddLanguageCommand {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String proficiency;
}
