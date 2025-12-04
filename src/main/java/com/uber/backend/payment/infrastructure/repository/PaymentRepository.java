package com.uber.backend.payment.infrastructure.repository;

import com.uber.backend.payment.infrastructure.persistence.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

}
