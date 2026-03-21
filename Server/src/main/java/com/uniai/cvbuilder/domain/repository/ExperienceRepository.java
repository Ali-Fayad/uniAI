package com.uniai.cvbuilder.domain.repository;

import com.uniai.cvbuilder.domain.model.Experience;

import java.util.List;
import java.util.Optional;

public interface ExperienceRepository {

    Optional<Experience> findById(Long id);

    List<Experience> findByCvId(Long cvId);

    Experience save(Experience experience);

    void delete(Experience experience);

    void deleteByCvId(Long cvId);
}
