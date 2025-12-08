package com.uber.backend.ride.application.command;

import com.uber.backend.driver.domain.model.Driver;
import com.uber.backend.driver.domain.model.Vehicle;
import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.persistence.DriverMapper;
import com.uber.backend.driver.infrastructure.persistence.VehicleEntity;
import com.uber.backend.driver.infrastructure.persistence.VehicleMapper;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.driver.infrastructure.repository.VehicleRepository;
import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import com.uber.backend.ride.api.dto.RideAssignDto;
import com.uber.backend.ride.api.dto.RideRequestDto;
import com.uber.backend.ride.api.dto.RideResponseDto;
import com.uber.backend.ride.application.exception.DriverNotFoundException;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.application.query.RideQueryService;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.domain.model.Ride;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.persistence.RideMapper;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RideCommandService {

    private final RideQueryService rideQueryService;
    private final RideRepository rideRepository;
    private final PassengerRepository passengerRepository;
    private final RideMapper rideMapper;
    private final DriverMapper driverMapper;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final VehicleMapper vehicleMapper;

    public RideResponseDto createRideRequest(RideRequestDto dto, Long passengerId) {

        BigDecimal fare = rideQueryService.calculateFare(dto.getType(), dto.getStart(), dto.getEnd(), dto.getDurationMin(), dto.getDemandMultiplier());
        Ride ride = Ride.builder().status(RideStatus.REQUESTED).requestedAt(LocalDateTime.now()).fareAmount(fare).pickupLocation(dto.getStart()).dropoffLocation(dto.getEnd()).passengerId(passengerId).build();
        RideEntity rideEntity = rideMapper.toEntity(ride);
        PassengerEntity passengerEntity = passengerRepository.findById(passengerId).orElse(null);
        rideEntity.setPassenger(passengerEntity);

        rideRepository.save(rideEntity);

        Long rideId = rideEntity.getId();
        RideStatus status = rideEntity.getStatus();
        LocalDateTime requestedAt = rideEntity.getRequestedAt();

        return RideResponseDto.builder().rideId(rideId).passengerId(passengerId).status(status).requestedAt(requestedAt).price(fare).build();


    }

    public RideAssignDto assignDriverToRide(Long rideId, Long driverId, Long passengerId) {
        RideEntity rideEntity = rideRepository.findById(rideId).orElse(null);
        if (rideEntity == null) {
            throw new RideNotFoundException(rideId);
        }

        DriverEntity driverEntity = driverRepository.findById(driverId).orElseThrow(() -> new DriverNotFoundException(driverId));

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
        return RideAssignDto.builder().rideId(rideId).passengerId(rideEntity.getPassenger().getId()).status(RideStatus.ACCEPTED).requestedAt(rideEntity.getRequestedAt()).price(rideEntity.getFareAmount()).driver(driver).vehicle(vehicle).startTime(startTime).build();

    }

    public String cancelRide(Long rideId) {
        RideEntity rideEntity = rideRepository.findById(rideId).orElse(null);
        if (rideEntity == null) {
            throw new RideNotFoundException(rideId);
        }
        rideEntity.setStatus(RideStatus.CANCELLED);
        rideRepository.save(rideEntity);
        DriverEntity driverEntity = driverRepository.findById(rideEntity.getDriver().getId()).orElseThrow(()  -> new DriverNotFoundException(rideEntity.getDriver().getId()));
        driverEntity.setIsAvailable(true);
        driverRepository.save(driverEntity);

        return "Ride cancelled";
    }

}
