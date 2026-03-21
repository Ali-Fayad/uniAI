package com.uniai.cvbuilder.infrastructure.persistence.adapter;

import com.uniai.cvbuilder.domain.model.Certificate;
import com.uniai.cvbuilder.domain.repository.CertificateRepository;
import com.uniai.cvbuilder.infrastructure.persistence.repository.JpaCertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CertificateRepositoryAdapter implements CertificateRepository {

    private final JpaCertificateRepository jpaRepository;

    @Override
    public Optional<Certificate> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Certificate> findByCvId(Long cvId) {
        return jpaRepository.findByCvId(cvId);
    }

    @Override
    public Certificate save(Certificate certificate) {
        return jpaRepository.save(certificate);
    }

    @Override
    public void delete(Certificate certificate) {
        jpaRepository.delete(certificate);
    }

    @Override
    public void deleteByCvId(Long cvId) {
        jpaRepository.deleteByCvId(cvId);
    }
}
