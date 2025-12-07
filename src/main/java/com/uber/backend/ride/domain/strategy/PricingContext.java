package com.uber.backend.ride.domain.strategy;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;

@Component
public class PricingContext {

    private final Map<String, PricingStrategy> strategies;

    public PricingContext(Map<String, PricingStrategy> strategies) {
        this.strategies = strategies;
    }

    public BigDecimal calculateFare(String type, double distance, int duration, double multiplier) {
        PricingStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown ride type: " + type);
        }
        return strategy.calculateFare(distance, duration, multiplier);
    }
}
