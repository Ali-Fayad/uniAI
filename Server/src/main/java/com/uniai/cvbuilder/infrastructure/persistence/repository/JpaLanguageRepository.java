package com.uniai.cvbuilder.infrastructure.persistence.repository;

import com.uniai.cvbuilder.domain.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaLanguageRepository extends JpaRepository<Language, Long> {

    List<Language> findByCvId(Long cvId);

    void deleteByCvId(Long cvId);
}
