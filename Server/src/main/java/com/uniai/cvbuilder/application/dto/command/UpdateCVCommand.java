package com.uniai.cvbuilder.application.dto.command;

import java.util.List;

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
    private Long templateId;
    private String template;
    private List<String> sectionsOrder;
    private com.uniai.cvbuilder.application.dto.SelectedItemsDto selectedItems;
    private Boolean isDefault;
}
