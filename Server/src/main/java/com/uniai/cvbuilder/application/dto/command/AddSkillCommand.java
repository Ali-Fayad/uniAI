package com.uniai.cvbuilder.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command payload for adding or reordering a skill entry within a CV.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddSkillCommand {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String level;

    @PositiveOrZero
    private Integer order;
}
