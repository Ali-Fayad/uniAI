package com.uniai.cvbuilder.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Command payload for adding or updating a portfolio project entry on a CV.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProjectCommand {

    @NotBlank
    private String name;

    private String description;
    private String githubUrl;
    private String liveUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> technologies;
}
