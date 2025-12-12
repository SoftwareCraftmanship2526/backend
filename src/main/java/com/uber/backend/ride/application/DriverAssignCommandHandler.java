package com.uber.backend.ride.application;

import com.uber.backend.driver.domain.model.Driver;
import com.uber.backend.driver.domain.model.Vehicle;
import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.persistence.DriverMapper;
import com.uber.backend.driver.infrastructure.persistence.VehicleEntity;
import com.uber.backend.driver.infrastructure.persistence.VehicleMapper;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.driver.infrastructure.repository.VehicleRepository;
import com.uber.backend.ride.application.command.DriverAssignCommand;
import com.uber.backend.ride.application.command.RideAssignResult;
import com.uber.backend.ride.application.exception.DriverNotFoundException;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DriverAssignCommandHandler {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final RideRepository rideRepository;
    private final DriverMapper driverMapper;
    private final VehicleMapper vehicleMapper;

    public RideAssignResult handle(DriverAssignCommand command, Long passengerId) {
        RideEntity rideEntity = rideRepository.findById(command.rideId()).orElse(null);
        if (rideEntity == null) {
            throw new RideNotFoundException(command.rideId());
        }

        DriverEntity driverEntity = driverRepository.findById(command.driverId()).orElseThrow(() -> new DriverNotFoundException(command.driverId()));

        rideEntity.setDriver(driverEntity);
        rideEntity.setStatus(RideStatus.ACCEPTED);
        LocalDateTime startTime = LocalDateTime.now();
        rideEntity.setStartedAt(startTime);

        driverEntity.setIsAvailable(false);
        driverEntity.setCurrentLocation(rideEntity.getPickupLocation());

        Driver driver = driverMapper.toDomain(driverEntity);
        Long vehicleId = driver.getCurrentVehicleId();
        VehicleEntity vehicleEntity = vehicleRepository.findById(vehicleId).orElse(null);
        Vehicle vehicle = vehicleMapper.toDomain(vehicleEntity);
        rideEntity.setVehicle(vehicleEntity);

        rideRepository.save(rideEntity);
        driverRepository.save(driverEntity);
        return new RideAssignResult(rideEntity.getId(), passengerId, rideEntity.getStatus(), rideEntity.getRequestedAt(), rideEntity.getFareAmount(), driver, vehicle, rideEntity.getStartedAt());

    }
}
