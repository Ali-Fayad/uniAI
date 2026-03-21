package com.uniai.cvbuilder.infrastructure.persistence.repository;

import com.uniai.cvbuilder.domain.model.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUniversityRepository extends JpaRepository<University, Long> {

    boolean existsByNameAndCampusName(String name, String campusName);
}
