package com.uber.backend.ride.domain.strategy;

import com.uber.backend.ride.domain.enums.RideType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("UberPool")
public class UberPoolStrategy implements PricingStrategy {

    private static final BigDecimal BASE_FARE = new BigDecimal("1.50");
    private static final BigDecimal COST_PER_KM = new BigDecimal("0.80");
    private static final BigDecimal COST_PER_MIN = new BigDecimal("0.20");
    private static final BigDecimal MINIMUM_FARE = new BigDecimal("3.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.70"); // 30% discount

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
                .multiply(DISCOUNT_RATE) // Apply UberPool discount
                .multiply(multiplier);

        if (totalFare.compareTo(MINIMUM_FARE) < 0) {
            totalFare = MINIMUM_FARE;
        }

        return totalFare.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public RideType getRideType() {
        return RideType.UBER_POOL;
    }
}
