package com.uniai.cvbuilder.domain.repository;

import com.uniai.cvbuilder.domain.model.Certificate;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository abstraction for certificate entries linked to CVs.
 */
public interface CertificateRepository {

    Optional<Certificate> findById(Long id);

    List<Certificate> findByCvId(Long cvId);

    Certificate save(Certificate certificate);

    void delete(Certificate certificate);

    void deleteByCvId(Long cvId);
}
