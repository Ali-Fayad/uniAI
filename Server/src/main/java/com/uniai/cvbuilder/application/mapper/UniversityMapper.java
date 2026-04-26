package com.uniai.cvbuilder.application.mapper;

import com.uniai.cvbuilder.application.dto.response.UniversityResponse;
import com.uniai.cvbuilder.domain.model.University;

/**
 * Mapper for converting {@link University} entities to API response DTOs.
 */
public final class UniversityMapper {

    private UniversityMapper() {}

    public static UniversityResponse toResponse(University university) {
        return UniversityResponse.builder()
                .id(university.getId())
                .name(university.getName())
                .nameAr(university.getNameAr())
                .acronym(university.getAcronym())
                .latitude(university.getLatitude())
                .longitude(university.getLongitude())
                .campusName(university.getCampusName())
                .campusType(university.getCampusType())
                .build();
    }
}
