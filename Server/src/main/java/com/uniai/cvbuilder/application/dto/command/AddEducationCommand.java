package com.uniai.cvbuilder.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Command payload for adding or updating an education entry for a CV, including university,
 * degree, dates, and optional notes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddEducationCommand {

    private Long universityId;

    @NotBlank
    private String degree;

    @NotBlank
    private String fieldOfStudy;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;
    private String grade;
    private String description;
}
