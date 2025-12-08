package com.uber.backend.ride.application.query;

import com.uber.backend.ride.domain.port.DistanceCalculatorPort;
import com.uber.backend.ride.domain.strategy.PricingContext;
import com.uber.backend.shared.domain.valueobject.Location;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class RideQueryService {

    private final PricingContext pricingContext;
    private final DistanceCalculatorPort distanceCalculator;

    public RideQueryService(PricingContext pricingContext, DistanceCalculatorPort distanceCalculator) {
        this.pricingContext = pricingContext;
        this.distanceCalculator = distanceCalculator;
    }

    public Map<String, BigDecimal> priceRequest(Location start, Location end, int durationMin, double demandMultiplier) {
        double distanceKm = distanceCalculator.calculateDistance(start, end);
        return pricingContext.calculateAllFares(distanceKm, durationMin, demandMultiplier);
    }

    public BigDecimal calculateFare(String type, Location start, Location end, int durationMin, double demandMultiplier) {
        double distanceKm = distanceCalculator.calculateDistance(start, end);
        return pricingContext.calculateFare(type, distanceKm, durationMin, demandMultiplier);
    }
}
