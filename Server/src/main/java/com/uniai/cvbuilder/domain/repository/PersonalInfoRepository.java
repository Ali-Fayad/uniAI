package com.uniai.cvbuilder.domain.repository;

import com.uniai.cvbuilder.domain.model.PersonalInfo;

import java.util.Optional;

/**
 * Domain repository abstraction for persisting and retrieving personal info records keyed by user.
 */
public interface PersonalInfoRepository {

    Optional<PersonalInfo> findByUserId(Long userId);

    PersonalInfo save(PersonalInfo personalInfo);
}
