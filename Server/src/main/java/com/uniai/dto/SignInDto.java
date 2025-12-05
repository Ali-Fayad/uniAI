package com.uniai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignInDto {

    @NotBlank
    @Size(min = 2, max = 100)
    private String email;

    @NotBlank
    @Size(min = 2, max = 100)
    private String password;
}

