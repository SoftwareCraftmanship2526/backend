package com.uber.backend.ride.application;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.ride.application.command.DriverAcceptCommand;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.application.query.RideResult;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverAcceptCommandHandler {

    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public RideResult handle(DriverAcceptCommand command, Long driverId) {
        // Find the ride
        RideEntity rideEntity = rideRepository.findById(command.rideId())
                .orElseThrow(() -> new RideNotFoundException(command.rideId()));

        // Validate current status - allow both REQUESTED and INVITED
        if (rideEntity.getStatus() != RideStatus.REQUESTED && rideEntity.getStatus() != RideStatus.INVITED) {
            throw new IllegalArgumentException("Ride must be in REQUESTED or INVITED status to be accepted. Current status: " + rideEntity.getStatus());
        }

        // Find the driver
        DriverEntity driverEntity = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));

        // If ride is INVITED, verify the driver is the invited one
        if (rideEntity.getStatus() == RideStatus.INVITED) {
            if (rideEntity.getDriver() == null || !rideEntity.getDriver().getId().equals(driverId)) {
                throw new IllegalArgumentException("Cannot accept ride " + command.rideId() + ": This ride is assigned to a different driver");
            }
        } else {
            // If REQUESTED (no poller invitation yet), assign the driver
            rideEntity.setDriver(driverEntity);
        }

        // Set the vehicle from the driver's current vehicle
        if (driverEntity.getCurrentVehicle() != null) {
            rideEntity.setVehicle(driverEntity.getCurrentVehicle());
        }

        // Update status to ACCEPTED
        rideEntity.setStatus(RideStatus.ACCEPTED);
        rideEntity.setAcceptedAt(java.time.LocalDateTime.now());
        rideEntity.setInvitedAt(null);

        rideEntity = rideRepository.save(rideEntity);
        return mapToRideResult(rideEntity);
    }

    private RideResult mapToRideResult(RideEntity entity) {
        RideResult.PaymentInfo paymentInfo = null;
        if (entity.getPayment() != null) {
            paymentInfo = new RideResult.PaymentInfo(
                entity.getPayment().getId(),
                entity.getPayment().getAmount(),
                entity.getPayment().getMethod(),
                entity.getPayment().getStatus(),
                entity.getPayment().getTransactionId()
            );
        }

        return new RideResult(
            entity.getId(),
            entity.getPassenger() != null ? entity.getPassenger().getId() : null,
            entity.getDriver() != null ? entity.getDriver().getId() : null,
            entity.getPickupLocation() != null ? entity.getPickupLocation().address() : null,
            entity.getDropoffLocation() != null ? entity.getDropoffLocation().address() : null,
            entity.getRideType(),
            entity.getStatus(),
            entity.getRequestedAt(),
            entity.getStartedAt(),
            entity.getCompletedAt(),
            entity.getDistanceKm(),
            entity.getDurationMin(),
            entity.getDemandMultiplier(),
            paymentInfo
        );
    }
}
