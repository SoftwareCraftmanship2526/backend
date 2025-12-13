package com.uber.backend.auth.infrastructure.repository;

import com.uber.backend.auth.infrastructure.persistence.VerificationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCodeEntity, Long> {

    Optional<VerificationCodeEntity> findByEmailAndCodeAndVerifiedFalse(String email, String code);

    Optional<VerificationCodeEntity> findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(String email);
}
