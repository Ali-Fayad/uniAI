package com.uniai.cvbuilder.infrastructure.persistence.repository;

import com.uniai.cvbuilder.domain.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByCvId(Long cvId);

    void deleteByCvId(Long cvId);
}
