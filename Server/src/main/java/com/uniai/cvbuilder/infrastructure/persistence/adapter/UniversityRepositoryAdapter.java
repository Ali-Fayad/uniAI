package com.uniai.cvbuilder.infrastructure.persistence.adapter;

import com.uniai.cvbuilder.domain.model.University;
import com.uniai.cvbuilder.domain.repository.UniversityRepository;
import com.uniai.cvbuilder.infrastructure.persistence.repository.JpaUniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UniversityRepositoryAdapter implements UniversityRepository {

    private final JpaUniversityRepository jpaRepository;

    @Override
    public Optional<University> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<University> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existsByNameAndCampusName(String name, String campusName) {
        return jpaRepository.existsByNameAndCampusName(name, campusName);
    }

    @Override
    public University save(University university) {
        return jpaRepository.save(university);
    }
}
