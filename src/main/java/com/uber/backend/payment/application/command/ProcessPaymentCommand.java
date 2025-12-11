package com.uber.backend.payment.application.command;

import com.uber.backend.payment.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record ProcessPaymentCommand(
        @NotNull(message = "Ride ID is required")
        Long rideId,

        @NotNull(message = "Passenger ID is required")
        Long passengerId,

        @NotNull(message = "Driver ID is required")
        Long driverId,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {}
