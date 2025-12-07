package com.uber.backend.ride.infrastructure.adapter;

import com.uber.backend.ride.domain.port.DistanceCalculatorPort;
import com.uber.backend.shared.domain.valueobject.Location;
import org.springframework.stereotype.Component;

@Component
public class HaversineDistanceAdapter implements DistanceCalculatorPort {

    @Override
    public double calculateDistance(Location from, Location to) {
        double R = 6371; // km

        double dLat = Math.toRadians(to.latitude() - from.latitude());
        double dLon = Math.toRadians(to.longitude() - from.longitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(from.latitude()))
                * Math.cos(Math.toRadians(to.latitude()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
