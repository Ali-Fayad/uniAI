package com.uniai.cvbuilder.infrastructure.persistence.repository;

import com.uniai.cvbuilder.domain.model.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for university reference data.
 */
@Repository
public interface JpaUniversityRepository extends JpaRepository<University, Long> {

    boolean existsByNameAndCampusName(String name, String campusName);
}
