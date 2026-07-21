package com.uniai.feedback.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitFeedbackCommand {
    @Min(1) @Max(5)
    private Integer rating;
    @NotBlank
    @Size(max = 5000)
    private String content;
}
