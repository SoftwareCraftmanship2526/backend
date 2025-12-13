package com.uber.backend.payment.application.command;

import com.uber.backend.payment.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record ProcessPaymentCommand(
        @NotNull(message = "Ride ID is required")
        Long rideId,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {}
