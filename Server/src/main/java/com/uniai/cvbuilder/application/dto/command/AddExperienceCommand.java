package com.uniai.cvbuilder.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String position;

    @NotBlank
    private String company;

    private String location;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isCurrent;

    private String description;

    private List<String> achievements;
}
