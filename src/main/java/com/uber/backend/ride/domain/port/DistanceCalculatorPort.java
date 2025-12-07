package com.uber.backend.ride.domain.port;

import com.uber.backend.shared.domain.valueobject.Location;

public interface DistanceCalculatorPort {
    double calculateDistance(Location from, Location to);
}
