package com.uber.backend.payment.application.query;

import com.uber.backend.payment.domain.enums.PaymentMethod;
import com.uber.backend.payment.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

public record PaymentHistoryResult(
        List<PaymentRecord> payments
) {
    public record PaymentRecord(
            Long paymentId,
            Long rideId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            PaymentStatus status,
            String transactionId
    ) {}
}
