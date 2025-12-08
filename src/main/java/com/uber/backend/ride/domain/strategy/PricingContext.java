package com.uber.backend.ride.domain.strategy;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class PricingContext {

    private final Map<String, PricingStrategy> strategies;

    public PricingContext(Map<String, PricingStrategy> strategies) {
        this.strategies = strategies;
    }

    public Map<String, BigDecimal> calculateAllFares(double distance, int duration, double multiplier) {
        Map<String, BigDecimal> fares = new HashMap<>();

        for (Map.Entry<String, PricingStrategy> entry : strategies.entrySet()) {
            String type = entry.getKey();
            PricingStrategy strategy = entry.getValue();
            BigDecimal fare = strategy.calculateFare(distance, duration, multiplier);
            fares.put(type, fare);
        }

        return fares;
    }
}
