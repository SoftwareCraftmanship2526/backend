package com.uber.backend.domain.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pricing strategy for UberX rides - standard affordable option.
 */
@Component
public class UberXStrategy implements PricingStrategy {

    private static final BigDecimal BASE_FARE = new BigDecimal("2.50");
    private static final BigDecimal COST_PER_KM = new BigDecimal("1.20");
    private static final BigDecimal COST_PER_MIN = new BigDecimal("0.30");
    private static final BigDecimal MINIMUM_FARE = new BigDecimal("5.00");

    @Override
    public BigDecimal calculateFare(double distanceKm, int durationMin, double demandMultiplier) {
        BigDecimal distance = BigDecimal.valueOf(distanceKm);
        BigDecimal duration = BigDecimal.valueOf(durationMin);
        BigDecimal multiplier = BigDecimal.valueOf(demandMultiplier);

        BigDecimal distanceCost = distance.multiply(COST_PER_KM);
        BigDecimal timeCost = duration.multiply(COST_PER_MIN);

        BigDecimal totalFare = BASE_FARE
                .add(distanceCost)
                .add(timeCost)
                .multiply(multiplier);

        if (totalFare.compareTo(MINIMUM_FARE) < 0) {
            totalFare = MINIMUM_FARE;
        }

        return totalFare.setScale(2, RoundingMode.HALF_UP);
    }
}
