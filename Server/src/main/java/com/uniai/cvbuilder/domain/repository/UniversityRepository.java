package com.uniai.cvbuilder.domain.repository;

import com.uniai.cvbuilder.domain.model.University;

import java.util.List;
import java.util.Optional;

public interface UniversityRepository {

    Optional<University> findById(Long id);

    List<University> findAll();

    boolean existsByNameAndCampusName(String name, String campusName);

    University save(University university);
}
