package com.uniai.user.infrastructure.persistence.adapter;

import com.uniai.user.domain.model.VerifyCode;
import com.uniai.user.domain.repository.VerifyCodeRepository;
import com.uniai.user.domain.valueobject.VerificationCodeType;
import com.uniai.user.infrastructure.persistence.repository.VerifyCodeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA-backed implementation of the domain {@link VerifyCodeRepository} interface.
 */
@Repository
@RequiredArgsConstructor
public class VerifyCodeRepositoryAdapter implements VerifyCodeRepository {

    private final VerifyCodeJpaRepository jpaRepository;

    @Override
    public void deleteByUserIdAndType(Long userId, VerificationCodeType type) {
        jpaRepository.deleteByUserIdAndType(userId, type);
    }

    @Override
    public Optional<VerifyCode> findTopByUserIdAndType(Long userId, VerificationCodeType type) {
        return jpaRepository.findTopByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
    }

    @Override
    public VerifyCode save(VerifyCode verifyCode) {
        return jpaRepository.save(verifyCode);
    }

    @Override
    public void delete(VerifyCode verifyCode) {
        jpaRepository.delete(verifyCode);
    }
}
