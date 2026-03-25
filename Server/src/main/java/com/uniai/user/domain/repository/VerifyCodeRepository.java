package com.uniai.user.domain.repository;

import com.uniai.user.domain.model.VerifyCode;
import com.uniai.user.domain.valueobject.VerificationCodeType;

import java.util.Optional;

/**
 * Domain repository interface for VerifyCode.
 * Implementations live in the infrastructure layer.
 */
public interface VerifyCodeRepository {

    void deleteByUserIdAndType(Long userId, VerificationCodeType type);

    Optional<VerifyCode> findTopByUserIdAndType(Long userId, VerificationCodeType type);

    VerifyCode save(VerifyCode verifyCode);

    void delete(VerifyCode verifyCode);
}
