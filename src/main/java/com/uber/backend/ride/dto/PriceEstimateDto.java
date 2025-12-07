package com.uber.backend.ride.dto;

import java.math.BigDecimal;

public record PriceEstimateDto(
        BigDecimal estimatedFare,
        double distanceKm,
        int durationMin,
        double demandMultiplier,
        String rideType,
        String currency
) {}
