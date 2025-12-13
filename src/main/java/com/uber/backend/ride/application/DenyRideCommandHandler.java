package com.uber.backend.ride.application;

import com.uber.backend.ride.application.command.DenyRideCommand;
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
public class DenyRideCommandHandler {

    private final RideRepository rideRepository;

    @Transactional
    public RideResult handle(DenyRideCommand command, Long driverId) {
        // Find the ride
        RideEntity rideEntity = rideRepository.findById(command.rideId())
                .orElseThrow(() -> new RideNotFoundException(command.rideId()));

        // Validate current status - can only deny INVITED rides
        if (rideEntity.getStatus() != RideStatus.INVITED) {
            throw new IllegalArgumentException("Only INVITED rides can be denied. Current status: " + rideEntity.getStatus());
        }

        // Validate that the logged-in driver is the one invited
        if (rideEntity.getDriver() == null || !rideEntity.getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("Cannot deny ride " + command.rideId() + ": This ride is assigned to a different driver");
        }

        // Add driver to denied list to prevent re-invitation
        if (!rideEntity.getDeniedDriverIds().contains(driverId)) {
            rideEntity.getDeniedDriverIds().add(driverId);
        }

        // Update status to DENIED and clear driver assignment
        rideEntity.setStatus(RideStatus.DENIED);
        rideEntity.setDriver(null);  // Remove driver so poller can assign a new one

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
