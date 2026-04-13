package com.uniai.cvbuilder.application.dto.command;

import java.util.List;

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

    private Long templateId;

    private String template;

    private List<String> sectionsOrder;

    private com.uniai.cvbuilder.application.dto.SelectedItemsDto selectedItems;

    private com.uniai.cvbuilder.application.dto.ItemsOrderDto itemsOrder;

    private Boolean isDefault;
}
