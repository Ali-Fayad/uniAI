package com.uniai.catalog.application.dto.response;

import java.math.BigDecimal;

public record CampusCatalogResponse(
        Long id,
        String name,
        String campusType,
        String city,
        String locality,
        BigDecimal latitude,
        BigDecimal longitude
) {}
