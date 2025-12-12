package com.uber.backend.ride.application;

import com.uber.backend.ride.application.command.StartRideCommand;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.application.query.RideResult;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StartRideCommandHandler {

    private final RideRepository rideRepository;

    @Transactional
    public RideResult handle(StartRideCommand command, Long driverId) {
        // Find the ride
        RideEntity rideEntity = rideRepository.findById(command.rideId())
                .orElseThrow(() -> new RideNotFoundException(command.rideId()));

        // Validate current status
        if (rideEntity.getStatus() != RideStatus.ACCEPTED) {
            throw new IllegalArgumentException("Ride must be in ACCEPTED status to be started. Current status: " + rideEntity.getStatus());
        }

        // Validate that the logged-in driver is the one assigned to this ride
        if (rideEntity.getDriver() == null || !rideEntity.getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("Cannot start ride " + command.rideId() + ": This ride is assigned to a different driver");
        }

        // Update status and set startedAt
        rideEntity.setStatus(RideStatus.IN_PROGRESS);
        rideEntity.setStartedAt(LocalDateTime.now());

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
