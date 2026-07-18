package com.uniai.catalog.application.dto.response;

public record UniversityCatalogResponse(
        Long id,
        String name,
        String acronym,
        String nameAr,
        java.util.List<CampusCatalogResponse> campuses
) {
    public UniversityCatalogResponse(Long id, String name, String acronym, String nameAr) {
        this(id, name, acronym, nameAr, java.util.List.of());
    }
}
