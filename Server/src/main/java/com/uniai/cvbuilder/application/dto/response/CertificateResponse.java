package com.uniai.cvbuilder.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateResponse {
    private Long id;
    private Long cvId;
    private String name;
    private String issuer;
    private LocalDate date;
    private String credentialUrl;
}
