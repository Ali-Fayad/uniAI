package com.uniai.cvbuilder.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedItemsDto {
    private List<String> skillIds;
    private List<String> languageIds;
    private List<String> educationIds;
    private List<String> experienceIds;
    private List<String> projectIds;
    private List<String> certificateIds;
}
