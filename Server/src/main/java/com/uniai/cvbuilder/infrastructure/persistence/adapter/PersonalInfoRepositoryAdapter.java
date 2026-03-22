package com.uniai.cvbuilder.infrastructure.persistence.adapter;

import com.uniai.cvbuilder.domain.model.PersonalInfo;
import com.uniai.cvbuilder.domain.repository.PersonalInfoRepository;
import com.uniai.cvbuilder.infrastructure.persistence.repository.JpaPersonalInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA-backed implementation of {@link PersonalInfoRepository}.
 */
@Repository
@RequiredArgsConstructor
public class PersonalInfoRepositoryAdapter implements PersonalInfoRepository {

    private final JpaPersonalInfoRepository jpaRepository;

    @Override
    public Optional<PersonalInfo> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public PersonalInfo save(PersonalInfo personalInfo) {
        return jpaRepository.save(personalInfo);
    }
}
