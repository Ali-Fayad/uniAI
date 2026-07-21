package com.uniai.cvbuilder.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Command payload for attaching a certificate record to a CV. Validated by the controller
 * before being handed to the CV application service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCertificateCommand {

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 200)
    private String issuer;
    private LocalDate date;
    @Size(max = 2048)
    @Pattern(regexp = "^$|https?://\\S+$", message = "Credential URL must use HTTP or HTTPS")
    private String credentialUrl;
}
