package com.uniai.cvbuilder.infrastructure.persistence.repository;

import com.uniai.cvbuilder.domain.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for certificate entries linked to CVs.
 */
@Repository
public interface JpaCertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findByCvId(Long cvId);

    void deleteByCvId(Long cvId);
}
