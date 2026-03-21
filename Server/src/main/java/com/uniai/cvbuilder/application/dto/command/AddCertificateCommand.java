package com.uniai.cvbuilder.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCertificateCommand {

    @NotBlank
    private String name;

    private String issuer;
    private LocalDate date;
    private String credentialUrl;
}
