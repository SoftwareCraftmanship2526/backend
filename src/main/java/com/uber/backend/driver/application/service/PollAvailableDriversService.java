package com.uber.backend.driver.application.service;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import com.uber.backend.shared.domain.port.DistanceCalculatorPort;
import com.uber.backend.shared.domain.valueobject.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollAvailableDriversService {

    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;
    private final DistanceCalculatorPort distanceCalculator;

    @Transactional
    public void pollForAvailableDriversForAllRides() {

        // Find rides that need driver assignment: REQUESTED (new) or DENIED (driver rejected)
        List<RideEntity> requestedRides = rideRepository.findByStatusEquals(RideStatus.REQUESTED);
        List<RideEntity> deniedRides = rideRepository.findByStatusEquals(RideStatus.DENIED);

        // Combine both lists
        List<RideEntity> rides = new ArrayList<>();
        rides.addAll(requestedRides);
        rides.addAll(deniedRides);

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
                // Skip drivers who have already denied this ride
                if (ride.getDeniedDriverIds().contains(driver.getId())) {
                    continue;
                }

                Location driverLocation = driver.getCurrentLocation();
                if (driverLocation == null) continue;

                double distance = distanceCalculator.calculateDistance(startLocation, driverLocation);
                distanceFromStart.put(distance, driver);
            }

            if (!distanceFromStart.isEmpty()) {
                DriverEntity nearest = distanceFromStart.firstEntry().getValue();
                ride.setStatus(RideStatus.INVITED);
                ride.setDriver(nearest);
                ride.setVehicle(nearest.getCurrentVehicle());
                ride.setInvitedAt(LocalDateTime.now());
                DriverEntity driver = ride.getDriver();
                driver.setIsAvailable(false);
                driverRepository.save(driver);
                rideRepository.save(ride);
            }
        }

    }

}


