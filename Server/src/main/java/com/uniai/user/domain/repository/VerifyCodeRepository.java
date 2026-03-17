package com.uniai.user.domain.repository;

import com.uniai.user.domain.model.VerifyCode;
import com.uniai.user.domain.valueobject.VerificationCodeType;

import java.util.Optional;

/**
 * Domain repository interface for VerifyCode.
 * Implementations live in the infrastructure layer.
 */
public interface VerifyCodeRepository {

    void deleteByEmailAndType(String email, VerificationCodeType type);

    Optional<VerifyCode> findTopByEmailAndType(String email, VerificationCodeType type);

    VerifyCode save(VerifyCode verifyCode);

    void delete(VerifyCode verifyCode);
}
