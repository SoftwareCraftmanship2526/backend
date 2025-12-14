package com.uber.backend.ride.application;

import com.uber.backend.ride.application.command.CancelRideIfUnmatchedCommand;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.domain.model.Ride;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.persistence.RideMapper;
import com.uber.backend.ride.infrastructure.repository.RideRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CancelRideIfUnmatchedCommandHandler {

    private final RideRepository rideRepository;
    private final RideMapper rideMapper;

    public void handle(CancelRideIfUnmatchedCommand command) {
        RideEntity rideEntity = rideRepository.findById(command.rideId())
                .orElseThrow(() -> new RideNotFoundException(command.rideId()));

        Ride ride = rideMapper.toDomain(rideEntity);
        ride.cancelIfUnmatched();
        RideEntity cancelledRideEntity = rideMapper.toEntity(ride);
        rideRepository.save(cancelledRideEntity);
    }
}
