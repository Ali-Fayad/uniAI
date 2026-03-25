package com.uniai.catalog.infrastructure.persistence.repository;

import com.uniai.catalog.domain.model.LanguageCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LanguageCatalogJpaRepository extends JpaRepository<LanguageCatalog, Long> {

    @Query("""
        SELECT l
        FROM LanguageCatalog l
        WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(COALESCE(l.nativeName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        ORDER BY l.name ASC
        """)
    List<LanguageCatalog> searchByNameOrNativeName(@Param("search") String search);
}
