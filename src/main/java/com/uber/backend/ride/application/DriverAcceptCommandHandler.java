package com.uber.backend.ride.application;

import com.uber.backend.ride.application.command.DriverAcceptCommand;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverAcceptCommandHandler {

    private final RideRepository rideRepository;

    public RideEntity handle(DriverAcceptCommand command) {
        RideEntity rideEntity = rideRepository.findById(command.rideId()).orElseThrow(() -> new RideNotFoundException(command.rideId()));
        rideEntity.setStatus(RideStatus.ACCEPTED);
        return rideRepository.save(rideEntity);
    }
}
