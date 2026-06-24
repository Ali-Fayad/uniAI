package com.uniai.user.infrastructure.persistence.repository;

import com.uniai.user.domain.model.VerifyCode;
import com.uniai.user.domain.valueobject.VerificationCodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface VerifyCodeJpaRepository extends JpaRepository<VerifyCode, Long> {
    void deleteByUserIdAndType(Long userId, VerificationCodeType type);
    Optional<VerifyCode> findTopByUserIdAndTypeOrderByCreatedAtDesc(Long userId, VerificationCodeType type);
    boolean existsByUserIdAndTypeAndUsedTrue(Long userId, VerificationCodeType type);

    @Modifying
    @Query("update VerifyCode v set v.used = true where v.userId = :userId and v.type = :type and v.used = false")
    void markByUserIdAndTypeUsed(@Param("userId") Long userId, @Param("type") VerificationCodeType type);
}
