package com.uniai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uniai.model.VerifyCode;

@Repository
public interface VerifyCodeRepository extends JpaRepository<VerifyCode, Long> {

    VerifyCode findTopByEmailOrderByExpirationTimeDesc(String email);
    void deleteByEmail(String email);
}
