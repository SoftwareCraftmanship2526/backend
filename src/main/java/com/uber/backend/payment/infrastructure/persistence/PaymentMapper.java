package com.uber.backend.payment.infrastructure.persistence;

import com.uber.backend.payment.domain.model.Payment;
import com.uber.backend.payment.infrastructure.persistence.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toDomain(PaymentEntity entity) {
        if (entity == null) {
            return null;
        }

        return Payment.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .method(entity.getMethod())
                .status(entity.getStatus())
                .transactionId(entity.getTransactionId())
                .rideId(entity.getRide() != null ? entity.getRide().getId() : null)
                .build();
    }

    public PaymentEntity toEntity(Payment domain) {
        if (domain == null) {
            return null;
        }

        PaymentEntity entity = new PaymentEntity();
        entity.setId(domain.getId());
        entity.setAmount(domain.getAmount());
        entity.setMethod(domain.getMethod());
        entity.setStatus(domain.getStatus());
        entity.setTransactionId(domain.getTransactionId());
        return entity;
    }

    public void updateEntity(Payment domain, PaymentEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        entity.setAmount(domain.getAmount());
        entity.setMethod(domain.getMethod());
        entity.setStatus(domain.getStatus());
        entity.setTransactionId(domain.getTransactionId());
    }
}
