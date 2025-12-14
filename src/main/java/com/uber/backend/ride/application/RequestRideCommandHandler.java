package com.uber.backend.ride.application;

import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import com.uber.backend.ride.application.command.RequestRideCommand;
import com.uber.backend.ride.application.command.RideRequestResult;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.domain.enums.RideType;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import com.uber.backend.shared.domain.port.GeocodingPort;
import com.uber.backend.shared.domain.valueobject.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestRideCommandHandler {

    private final RideRepository rideRepository;
    private final PassengerRepository passengerRepository;
    private final GeocodingPort geocodingPort;
    private final ApplicationEventPublisher publisher;

    public RideRequestResult handle(RequestRideCommand command, Long passengerId) {

        // Get ride type from command (already enum)
        RideType rideType = command.rideType();

        // Check if passenger has an active ride (REQUESTED, INVITED, ACCEPTED, or IN_PROGRESS)
        List<RideEntity> activeRides = rideRepository.findByPassengerIdAndStatusIn(
            passengerId,
            List.of(RideStatus.REQUESTED, RideStatus.INVITED, RideStatus.ACCEPTED, RideStatus.IN_PROGRESS)
        );

        if (!activeRides.isEmpty()) {
            RideEntity activeRide = activeRides.get(0);
            throw new IllegalArgumentException(
                "Cannot request a new ride. You already have an active ride (ID: " + activeRide.getId() + 
                ", Status: " + activeRide.getStatus() + "). Please complete or cancel your current ride first."
            );
        }

        // Find passenger
        PassengerEntity passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found: " + passengerId));

        // Resolve pickup address from coordinates
        String pickupAddress = geocodingPort.getAddressFromCoordinates(
            command.pickupLatitude(), 
            command.pickupLongitude()
        );

        // Create pickup location
        Location pickupLocation = new Location(
            command.pickupLatitude(),
            command.pickupLongitude(),
            pickupAddress
        );

        // Resolve dropoff address from coordinates
        String dropoffAddress = geocodingPort.getAddressFromCoordinates(
            command.dropoffLatitude(), 
            command.dropoffLongitude()
        );

        // Create dropoff location
        Location dropoffLocation = new Location(
            command.dropoffLatitude(),
            command.dropoffLongitude(),
            dropoffAddress
        );

        // Create ride entity
        RideEntity rideEntity = new RideEntity();
        rideEntity.setStatus(RideStatus.REQUESTED);
        rideEntity.setRequestedAt(LocalDateTime.now());
        rideEntity.setPickupLocation(pickupLocation);
        rideEntity.setDropoffLocation(dropoffLocation);
        rideEntity.setPassenger(passenger);
        rideEntity.setRideType(rideType);

        // Save ride entity
        rideEntity = rideRepository.save(rideEntity);

        RideRequestResult result = new RideRequestResult(rideEntity.getId(), passengerId, rideEntity.getStatus(), rideEntity.getRequestedAt());
        publisher.publishEvent(result);

        // Return result (price is null at request time, calculated when ride completes)
        return result;


    }
}
