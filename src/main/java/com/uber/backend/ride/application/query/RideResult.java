package com.uber.backend.ride.application.query;

import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.domain.enums.RideType;
import com.uber.backend.payment.domain.enums.PaymentStatus;
import com.uber.backend.payment.domain.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideResult(
        Long rideId,
        Long passengerId,
        Long driverId,
        String pickupAddress,
        String dropoffAddress,
        RideType rideType,
        RideStatus status,
        LocalDateTime requestedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        Double distanceKm,
        Integer durationMin,
        Double demandMultiplier,
        PaymentInfo payment
) {
    public record PaymentInfo(
            Long paymentId,
            BigDecimal amount,
            PaymentMethod method,
            PaymentStatus status,
            String transactionId
    ) {}
}
