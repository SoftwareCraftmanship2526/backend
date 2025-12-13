package com.uber.backend.ride.application;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import com.uber.backend.ride.application.command.CreateRideCommand;
import com.uber.backend.ride.application.command.CreateRideResult;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import com.uber.backend.shared.domain.valueobject.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Command handler for creating rides.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateRideCommandHandler {

    private final RideRepository rideRepository;
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public CreateRideResult handle(CreateRideCommand command) {
        log.info("Creating ride for passenger: {}", command.passengerId());

        // Get passenger
        PassengerEntity passenger = passengerRepository.findById(command.passengerId())
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found: " + command.passengerId()));

        // Get driver if provided
        DriverEntity driver = null;
        if (command.driverId() != null) {
            driver = driverRepository.findById(command.driverId())
                    .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + command.driverId()));
        }

        // Create pickup location
        Location pickupLocation = new Location(
                command.pickupLatitude(),
                command.pickupLongitude(),
                command.pickupAddress()
        );

        // Create dropoff location
        Location dropoffLocation = new Location(
                command.dropoffLatitude(),
                command.dropoffLongitude(),
                command.dropoffAddress()
        );

        // Create ride entity
        RideEntity ride = RideEntity.builder()
                .passenger(passenger)
                .driver(driver)
                .pickupLocation(pickupLocation)
                .dropoffLocation(dropoffLocation)
                .rideType(command.rideType())
                .status(command.status())
                .build();

        // Set timestamps based on status
        if (command.status() == RideStatus.IN_PROGRESS) {
            ride.setStartedAt(LocalDateTime.now());
        } else if (command.status() == RideStatus.COMPLETED) {
            ride.setStartedAt(LocalDateTime.now().minusMinutes(30)); // Mock: ride started 30 min ago
            ride.setCompletedAt(LocalDateTime.now());
        }

        ride = rideRepository.save(ride);

        // Payment will be created when ride status changes to COMPLETED via UpdateRideStatus
        String message = "Ride created successfully";

        return new CreateRideResult(
                ride.getId(),
                passenger.getId(),
                driver != null ? driver.getId() : null,
                command.pickupAddress(),
                command.dropoffAddress(),
                ride.getStatus(),
                message
        );
    }
}
