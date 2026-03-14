package com.uniai.user.infrastructure.persistence.repository;

import com.uniai.user.domain.model.VerifyCode;
import com.uniai.user.domain.valueobject.VerificationCodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerifyCodeJpaRepository extends JpaRepository<VerifyCode, Long> {
    void deleteByEmailAndType(String email, VerificationCodeType type);
    Optional<VerifyCode> findTopByEmailAndTypeOrderByExpirationTimeDesc(String email, VerificationCodeType type);
}
