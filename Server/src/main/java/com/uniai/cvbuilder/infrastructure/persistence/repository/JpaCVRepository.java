package com.uniai.cvbuilder.infrastructure.persistence.repository;

import com.uniai.cvbuilder.domain.model.CV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCVRepository extends JpaRepository<CV, Long> {

    List<CV> findByUserId(Long userId);

    Optional<CV> findFirstByUserIdAndIsDefaultTrue(Long userId);
}
