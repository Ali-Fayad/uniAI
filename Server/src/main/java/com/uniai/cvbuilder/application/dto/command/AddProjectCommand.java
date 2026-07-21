package com.uniai.cvbuilder.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 150)
    private String name;

    @Size(max = 5000)
    private String description;
    @Size(max = 2048)
    @Pattern(regexp = "^$|https?://\\S+$", message = "GitHub URL must use HTTP or HTTPS")
    private String githubUrl;
    @Size(max = 2048)
    @Pattern(regexp = "^$|https?://\\S+$", message = "Live URL must use HTTP or HTTPS")
    private String liveUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> technologies;
}
