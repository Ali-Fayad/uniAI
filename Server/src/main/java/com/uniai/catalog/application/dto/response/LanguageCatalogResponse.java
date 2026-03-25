package com.uniai.catalog.application.dto.response;

public record LanguageCatalogResponse(
        Long id,
        String name,
        String code,
        String nativeName
) {}
