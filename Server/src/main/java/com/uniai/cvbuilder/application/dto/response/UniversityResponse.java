package com.uniai.cvbuilder.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UniversityResponse {
    private Long id;
    private String name;
    private String nameAr;
    private String acronym;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String campusName;
    private String campusType;
}
