package com.uber.backend.payment.application;

import com.uber.backend.payment.application.query.CalculateFareQuery;
import com.uber.backend.payment.application.query.FareCalculationResult;
import com.uber.backend.ride.domain.enums.RideType;
import com.uber.backend.ride.domain.strategy.PricingStrategy;
import com.uber.backend.ride.domain.strategy.UberBlackStrategy;
import com.uber.backend.ride.domain.strategy.UberPoolStrategy;
import com.uber.backend.ride.domain.strategy.UberXStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Query handler for fare calculation.
 * Contains business logic for calculating ride fares using pricing strategies.
 * Follows CQRS pattern - this handler is responsible for the "read" operation.
 */
@Component
@RequiredArgsConstructor
public class CalculateFareQueryHandler {

    private final UberXStrategy uberXStrategy;
    private final UberBlackStrategy uberBlackStrategy;
    private final UberPoolStrategy uberPoolStrategy;

    /**
     * Handle fare calculation query.
     * Uses the strategy pattern to calculate fare based on ride type.
     *
     * @param query Contains distance, duration, ride type, and demand multiplier
     * @return Fare calculation result with total and detailed breakdown
     */
    public FareCalculationResult handle(CalculateFareQuery query) {
        // 1. Get the appropriate pricing strategy for the ride type
        PricingStrategy strategy = getStrategyForRideType(query.rideType());

        // 2. Calculate total fare using the strategy
        BigDecimal totalFare = strategy.calculateFare(
                query.distanceKm(),
                query.durationMin(),
                query.demandMultiplier()
        );

        // 3. Calculate detailed breakdown for transparency
        FareCalculationResult.FareBreakdown breakdown = calculateBreakdown(
                query.distanceKm(),
                query.durationMin(),
                query.rideType(),
                query.demandMultiplier(),
                totalFare
        );

        // 4. Return complete result
        return new FareCalculationResult(
                query.rideType(),
                query.distanceKm(),
                query.durationMin(),
                query.demandMultiplier(),
                totalFare,
                "EUR",
                breakdown
        );
    }

    /**
     * Select the correct pricing strategy based on ride type.
     * Each ride type has different pricing rules.
     */
    private PricingStrategy getStrategyForRideType(RideType rideType) {
        return switch (rideType) {
            case UBER_X -> uberXStrategy;      // Standard pricing
            case UBER_BLACK -> uberBlackStrategy; // Premium pricing
            case UBER_POOL -> uberPoolStrategy;   // Discounted pricing
        };
    }

    /**
     * Calculate detailed breakdown of fare components.
     * Shows passenger exactly what they're paying for.
     */
    private FareCalculationResult.FareBreakdown calculateBreakdown(
            Double distanceKm, Integer durationMin, RideType rideType,
            Double demandMultiplier, BigDecimal totalFare) {

        // Get base rates for this ride type
        BigDecimal baseFare = getBaseFareForRideType(rideType);
        BigDecimal perKmRate = getPerKmRateForRideType(rideType);
        BigDecimal perMinRate = getPerMinRateForRideType(rideType);

        // Calculate distance cost
        BigDecimal distanceFare = BigDecimal.valueOf(distanceKm)
                .multiply(perKmRate)
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate duration cost
        BigDecimal durationFare = BigDecimal.valueOf(durationMin)
                .multiply(perMinRate)
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate subtotal before discounts/multipliers
        BigDecimal subtotal = baseFare
                .add(distanceFare)
                .add(durationFare)
                .setScale(2, RoundingMode.HALF_UP);

        // Apply discount for UberPool (30% off)
        BigDecimal discount = BigDecimal.ZERO;
        if (rideType == RideType.UBER_POOL) {
            discount = subtotal.multiply(BigDecimal.valueOf(0.30))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal subtotalAfterDiscount = subtotal.subtract(discount);

        // Calculate surge pricing amount (if demand multiplier > 1.0)
        BigDecimal demandMultiplierAmount = BigDecimal.ZERO;
        if (demandMultiplier > 1.0) {
            demandMultiplierAmount = subtotalAfterDiscount
                    .multiply(BigDecimal.valueOf(demandMultiplier - 1.0))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new FareCalculationResult.FareBreakdown(
                baseFare,
                distanceFare,
                durationFare,
                subtotal,
                demandMultiplierAmount,
                discount,
                totalFare
        );
    }

    // Base fares for each ride type
    private BigDecimal getBaseFareForRideType(RideType rideType) {
        return switch (rideType) {
            case UBER_X -> new BigDecimal("2.50");
            case UBER_BLACK -> new BigDecimal("8.00");
            case UBER_POOL -> new BigDecimal("1.50");
        };
    }

    // Per kilometer rates for each ride type
    private BigDecimal getPerKmRateForRideType(RideType rideType) {
        return switch (rideType) {
            case UBER_X -> new BigDecimal("1.20");
            case UBER_BLACK -> new BigDecimal("3.50");
            case UBER_POOL -> new BigDecimal("0.80");
        };
    }

    // Per minute rates for each ride type
    private BigDecimal getPerMinRateForRideType(RideType rideType) {
        return switch (rideType) {
            case UBER_X -> new BigDecimal("0.30");
            case UBER_BLACK -> new BigDecimal("0.80");
            case UBER_POOL -> new BigDecimal("0.20");
        };
    }
}
