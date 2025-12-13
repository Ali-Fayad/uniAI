package com.uniai.repository;

import com.uniai.model.VerifyCode;
import com.uniai.domain.VerificationCodeType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerifyCodeRepository extends JpaRepository<VerifyCode, Long> {

    // Delete any existing codes for the email+type before creating a new one
    void deleteByEmailAndType(String email, VerificationCodeType type);

    // Find the most recent code for this email+type
    VerifyCode findTopByEmailAndTypeOrderByExpirationTimeDesc(String email, VerificationCodeType type);
}
