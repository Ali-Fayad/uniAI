package com.uniai.catalog.domain.repository;

import com.uniai.catalog.domain.model.UniversityCatalog;
import java.util.List;

public interface UniversityCatalogRepository {
    List<UniversityCatalog> findAll();

    List<UniversityCatalog> searchByName(String search);
}
