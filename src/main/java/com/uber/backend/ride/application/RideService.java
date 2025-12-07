package com.uber.backend.ride.application;

import com.uber.backend.ride.domain.port.DistanceCalculatorPort;
import com.uber.backend.ride.domain.strategy.PricingContext;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import com.uber.backend.shared.domain.valueobject.Location;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RideService {

    private final PricingContext pricingContext;
    private final DistanceCalculatorPort distanceCalculator;

    public RideService(RideRepository rideRepository,
                       PricingContext pricingContext,
                       DistanceCalculatorPort distanceCalculator) {
        this.pricingContext = pricingContext;
        this.distanceCalculator = distanceCalculator;
    }

    public BigDecimal calculatePrice(String type,
                                     Location start,
                                     Location end,
                                     int durationMin,
                                     double demandMultiplier) {

        double distanceKm = distanceCalculator.calculateDistance(start, end);

        return pricingContext.calculateFare(type, distanceKm, durationMin, demandMultiplier);
    }
}
