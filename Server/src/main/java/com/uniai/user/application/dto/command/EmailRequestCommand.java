package com.uniai.user.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestCommand {

    @NotBlank
    @Email
    @Size(min = 2, max = 100)
    private String email;
}
