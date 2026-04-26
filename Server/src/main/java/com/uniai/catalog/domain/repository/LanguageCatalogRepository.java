package com.uniai.catalog.domain.repository;

import com.uniai.catalog.domain.model.LanguageCatalog;
import java.util.List;

public interface LanguageCatalogRepository {
    List<LanguageCatalog> findAll();

    List<LanguageCatalog> searchByNameOrNativeName(String search);
}
