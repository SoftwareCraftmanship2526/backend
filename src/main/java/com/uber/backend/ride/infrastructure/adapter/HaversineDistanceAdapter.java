package com.uber.backend.ride.infrastructure.adapter;

import com.uber.backend.ride.domain.port.DistanceCalculatorPort;
import com.uber.backend.shared.domain.valueobject.Location;
import org.springframework.stereotype.Component;

@Component
public class HaversineDistanceAdapter implements DistanceCalculatorPort {

    @Override
    public double calculateDistance(Location start, Location end) {
        double R = 6371; // km

        double dLat = Math.toRadians(end.latitude() - start.latitude());
        double dLon = Math.toRadians(end.longitude() - start.longitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(start.latitude()))
                * Math.cos(Math.toRadians(end.latitude()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}