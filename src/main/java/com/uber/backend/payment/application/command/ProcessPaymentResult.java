package com.uber.backend.payment.application.command;

import com.uber.backend.payment.domain.enums.PaymentMethod;
import com.uber.backend.payment.domain.enums.PaymentStatus;

import java.math.BigDecimal;

public record ProcessPaymentResult(
        Long paymentId,
        Long rideId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String transactionId
) {}
