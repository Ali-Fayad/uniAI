package com.uniai.cvbuilder.application.dto.command;

import jakarta.validation.constraints.NotBlank;
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
    private String name;

    private String level;

    private Integer order;
}
