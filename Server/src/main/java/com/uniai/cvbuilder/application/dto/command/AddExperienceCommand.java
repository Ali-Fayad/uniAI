package com.uniai.cvbuilder.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Command payload for adding or updating a professional experience block for a CV, including
 * role details, duration, and achievements.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddExperienceCommand {

    @NotBlank
    @Size(max = 150)
    private String position;

    @NotBlank
    @Size(max = 200)
    private String company;

    @Size(max = 200)
    private String location;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isCurrent;

    @Size(max = 5000)
    private String description;

    @Size(max = 50)
    private List<String> achievements;
}
