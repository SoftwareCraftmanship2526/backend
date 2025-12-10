package com.uber.backend.payment.application.query;

import com.uber.backend.ride.domain.enums.RideType;

import java.math.BigDecimal;

public record FareCalculationResult(
        RideType rideType,
        Double distanceKm,
        Integer durationMin,
        Double demandMultiplier,
        BigDecimal totalFare,
        String currency,
        FareBreakdown breakdown
) {
    public record FareBreakdown(
            BigDecimal baseFare,
            BigDecimal distanceFare,
            BigDecimal durationFare,
            BigDecimal subtotal,
            BigDecimal demandMultiplierAmount,
            BigDecimal discount,
            BigDecimal total
    ) {}
}
