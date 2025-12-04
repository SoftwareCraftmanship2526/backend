package com.uber.backend.domain.strategy;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculateFare(double distanceKm, int durationMin, double demandMultiplier);
}
