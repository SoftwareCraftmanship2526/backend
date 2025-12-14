package com.uber.backend.ride.application;

import com.uber.backend.ride.application.command.CancelRideIfUnmatchedCommand;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;

import com.uber.backend.ride.infrastructure.repository.RideRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class CancelRideIfUnmatchedCommandHandler {

    private final RideRepository rideRepository;

    public void handle(CancelRideIfUnmatchedCommand command) {
        RideEntity ride = rideRepository.findById(command.rideId())
                .orElseThrow(() -> new RideNotFoundException(command.rideId()));

        ride.cancelIfUnmatched();
        // no save() needed
    }
}

