package com.uniai.user.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInCommand {

    @NotBlank
    @Size(min = 2, max = 100)
    private String email;

    @NotBlank
    @Size(min = 2, max = 100)
    private String password;
}
