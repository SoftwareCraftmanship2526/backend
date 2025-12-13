package com.uber.backend.ride.domain.strategy;

import com.uber.backend.ride.domain.enums.RideType;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class PricingContext {

    private final Map<RideType, PricingStrategy> strategies = new HashMap<>();

    public PricingContext(Map<String, PricingStrategy> strategyBeans) {
        strategyBeans.forEach((beanName, strategy) -> {
            RideType rideType = strategy.getRideType();
            strategies.put(rideType, strategy);
        });
    }

    public Map<RideType, BigDecimal> calculateAllFares(
            double distance,
            int duration,
            double multiplier
    ) {
        Map<RideType, BigDecimal> fares = new HashMap<>();

        for (Map.Entry<RideType, PricingStrategy> entry : strategies.entrySet()) {
            fares.put(
                    entry.getKey(),
                    entry.getValue().calculateFare(distance, duration, multiplier)
            );
        }
        return fares;
    }

    public BigDecimal calculateFare(
            RideType type,
            double distance,
            int duration,
            double multiplier
    ) {
        PricingStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No pricing strategy for ride type: " + type);
        }
        return strategy.calculateFare(distance, duration, multiplier);
    }
}
