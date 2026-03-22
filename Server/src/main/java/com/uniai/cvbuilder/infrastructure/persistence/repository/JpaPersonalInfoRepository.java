package com.uniai.cvbuilder.infrastructure.persistence.repository;

import com.uniai.cvbuilder.domain.model.PersonalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data repository for user personal info records.
 */
@Repository
public interface JpaPersonalInfoRepository extends JpaRepository<PersonalInfo, Long> {

    Optional<PersonalInfo> findByUserId(Long userId);
}
