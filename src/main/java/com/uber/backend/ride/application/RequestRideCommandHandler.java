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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RequestRideCommandHandler {

    private final RideRepository rideRepository;
    private final PassengerRepository passengerRepository;
    private final GeocodingPort geocodingPort;

    public RideRequestResult handle(RequestRideCommand command, Long passengerId) {

        // Get ride type from command (already enum)
        RideType rideType = command.rideType();


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

        // Return result (price is null at request time, calculated when ride completes)
        return new RideRequestResult(
            rideEntity.getId(),
            passengerId,
            rideEntity.getStatus(),
            rideEntity.getRequestedAt()
        );


    }
}
