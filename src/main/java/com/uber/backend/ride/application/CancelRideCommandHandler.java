package com.uber.backend.ride.application;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.ride.application.command.CancelRideCommand;
import com.uber.backend.ride.application.exception.DriverNotFoundException;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelRideCommandHandler {
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    public String handle(CancelRideCommand command) {
        RideEntity rideEntity = rideRepository.findById(command.rideId()).orElse(null);
        if (rideEntity == null) {
            throw new RideNotFoundException(command.rideId());
        }
        rideEntity.setStatus(RideStatus.CANCELLED);
        rideRepository.save(rideEntity);
        DriverEntity driverEntity = driverRepository.findById(rideEntity.getDriver().getId()).orElseThrow(()  -> new DriverNotFoundException(rideEntity.getDriver().getId()));
        driverEntity.setIsAvailable(true);
        driverRepository.save(driverEntity);
        return "Ride canceled";
    }
}
