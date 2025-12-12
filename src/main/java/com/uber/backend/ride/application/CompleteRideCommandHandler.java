package com.uber.backend.ride.application;

import com.uber.backend.ride.application.command.CompleteRideCommand;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompleteRideCommandHandler {
    private final RideRepository rideRepository;
    public RideEntity handle(CompleteRideCommand command) {
        RideEntity rideEntity = rideRepository.findById(command.rideId()).orElse(null);
        if (rideEntity == null) {
            throw new RideNotFoundException(command.rideId().toString());
        }
        rideEntity.setStatus(RideStatus.COMPLETED);
        rideRepository.save(rideEntity);
        return rideEntity;
    }
}
