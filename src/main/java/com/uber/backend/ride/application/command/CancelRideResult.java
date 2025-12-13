package com.uber.backend.ride.application.command;

import com.uber.backend.payment.domain.enums.PaymentMethod;
import com.uber.backend.payment.domain.enums.PaymentStatus;

import java.math.BigDecimal;

public record CancelRideResult(
        String message,
        PaymentInfo payment
) {
    public record PaymentInfo(
            Long paymentId,
            BigDecimal amount,
            PaymentMethod method,
            PaymentStatus status
    ) {}
}
