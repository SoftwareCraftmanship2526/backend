package com.uber.backend.domain.strategy;

import java.math.BigDecimal;

/**
 * Strategy Pattern interface for calculating ride fares.
 * Implementations provide different pricing algorithms based on ride type.
 */
public interface PricingStrategy {

    /**
     * Calculates the fare for a ride based on distance, duration, and demand multiplier.
     *
     * @param distanceKm The distance of the ride in kilometers
     * @param durationMin The duration of the ride in minutes
     * @param demandMultiplier The surge pricing multiplier (1.0 = normal, > 1.0 = surge)
     * @return The calculated fare amount
     */
    BigDecimal calculateFare(double distanceKm, int durationMin, double demandMultiplier);
}
