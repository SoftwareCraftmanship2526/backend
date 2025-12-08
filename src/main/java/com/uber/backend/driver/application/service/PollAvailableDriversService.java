package com.uber.backend.driver.application.service;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import com.uber.backend.shared.domain.port.DistanceCalculatorPort;
import com.uber.backend.shared.domain.valueobject.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PollAvailableDriversService {

    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;
    private final DistanceCalculatorPort distanceCalculator;

    public void pollForAvailableDriversForAllRides() {

        List<RideEntity> rides = rideRepository.findByStatusEquals(RideStatus.REQUESTED);

        for (RideEntity ride : rides) {
            List<DriverEntity> availableDrivers = driverRepository.findByIsAvailableTrue();
            if (availableDrivers.isEmpty()) {
                return;
            }

            Location startLocation = ride.getPickupLocation();
            if (startLocation == null) {
                continue;
            }

            TreeMap<Double, DriverEntity> distanceFromStart = new TreeMap<>();

            for (DriverEntity driver : availableDrivers) {
                Location driverLocation = driver.getCurrentLocation();
                if (driverLocation == null) continue;

                double distance = distanceCalculator.calculateDistance(startLocation, driverLocation);
                distanceFromStart.put(distance, driver);
            }

            if (!distanceFromStart.isEmpty()) {
                DriverEntity nearest = distanceFromStart.firstEntry().getValue();
                ride.setStatus(RideStatus.INVITED);
                ride.setDriver(nearest);
                rideRepository.save(ride);
            }
        }

    }

}


