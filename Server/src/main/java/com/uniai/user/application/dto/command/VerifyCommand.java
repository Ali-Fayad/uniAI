package com.uniai.user.application.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCommand {
    @NotBlank @Email @Size(max = 100)
    private String email;
    @NotBlank @Size(min = 6, max = 128)
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Verification code contains invalid characters")
    private String verificationCode;
}
