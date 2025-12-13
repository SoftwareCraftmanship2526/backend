package com.uber.backend.ride.domain.strategy;

import com.uber.backend.ride.domain.enums.RideType;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculateFare(double distanceKm, int durationMin, double demandMultiplier);
    RideType getRideType();
}
