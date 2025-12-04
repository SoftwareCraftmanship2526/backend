package com.uber.backend.ride.domain.strategy;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculateFare(double distanceKm, int durationMin, double demandMultiplier);
}
