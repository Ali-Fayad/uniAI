package com.uniai.cvbuilder.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Command payload for updating CV metadata such as name, template choice, and default status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCVCommand {

    private String cvName;
    private Long templateId;
    private String template;
    private List<String> sectionsOrder;
    private Boolean isDefault;
}
