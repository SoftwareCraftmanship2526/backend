package com.uber.backend.ride.application;

import com.uber.backend.ride.domain.strategy.PricingStrategy;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final Map<String, PricingStrategy> pricingStrategies;

    public RideService(RideRepository rideRepository, Map<String, PricingStrategy> pricingStrategies) {
        this.rideRepository = rideRepository;
        this.pricingStrategies = pricingStrategies;
    }

    public BigDecimal calculatePrice(String type, double distanceKm, int durationMin, double demandMultiplier) {
        PricingStrategy strategy = pricingStrategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException(String.format("Invalid type %s", type));
        }
        return strategy.calculateFare(distanceKm, durationMin, demandMultiplier);
    }
}
