package com.uniai.cvbuilder.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command payload for updating CV metadata such as name, template choice, and default status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCVCommand {

    private String cvName;
    private String template;
    private Boolean isDefault;
}
