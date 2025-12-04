package com.uber.backend.infrastructure.repository;

import com.uber.backend.domain.enums.PaymentStatus;
import com.uber.backend.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByRideId(Long rideId);

    List<PaymentEntity> findByStatus(PaymentStatus status);
}
