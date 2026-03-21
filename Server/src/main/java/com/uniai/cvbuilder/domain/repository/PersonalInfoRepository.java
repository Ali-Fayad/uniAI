package com.uniai.cvbuilder.domain.repository;

import com.uniai.cvbuilder.domain.model.PersonalInfo;

import java.util.Optional;

public interface PersonalInfoRepository {

    Optional<PersonalInfo> findByUserId(Long userId);

    PersonalInfo save(PersonalInfo personalInfo);
}
