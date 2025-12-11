package com.uber.backend.ride.application;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.payment.application.CalculateFareQueryHandler;
import com.uber.backend.payment.application.query.CalculateFareQuery;
import com.uber.backend.payment.application.query.FareCalculationResult;
import com.uber.backend.payment.domain.enums.PaymentStatus;
import com.uber.backend.payment.infrastructure.persistence.PaymentEntity;
import com.uber.backend.payment.infrastructure.repository.PaymentRepository;
import com.uber.backend.ride.application.command.UpdateRideStatusCommand;
import com.uber.backend.ride.application.command.UpdateRideStatusResult;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Command handler for updating ride status.
 * Automatically creates a pending payment when ride status changes to COMPLETED.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateRideStatusCommandHandler {

    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final PaymentRepository paymentRepository;
    private final CalculateFareQueryHandler calculateFareQueryHandler;

    @Transactional
    public UpdateRideStatusResult handle(UpdateRideStatusCommand command) {
        log.info("Updating ride status: rideId={}, newStatus={}", command.rideId(), command.newStatus());

        // Get ride
        RideEntity ride = rideRepository.findById(command.rideId())
                .orElseThrow(() -> new IllegalArgumentException("Ride not found: " + command.rideId()));

        RideStatus oldStatus = ride.getStatus();

        // Validate status transition
        validateStatusTransition(oldStatus, command.newStatus());

        // Handle ACCEPTED: assign driver
        if (command.newStatus() == RideStatus.ACCEPTED) {
            if (command.driverId() == null) {
                throw new IllegalArgumentException("Driver ID is required when accepting a ride");
            }
            DriverEntity driver = driverRepository.findById(command.driverId())
                    .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + command.driverId()));
            ride.setDriver(driver);
            log.info("Driver assigned to ride: driverId={}", command.driverId());
        }

        // Handle IN_PROGRESS: set start time
        if (command.newStatus() == RideStatus.IN_PROGRESS) {
            ride.setStartedAt(LocalDateTime.now());
            log.info("Ride started: rideId={}", command.rideId());
        }

        // Handle COMPLETED: set completion time, calculate fare, and create pending payment
        String message = "Ride status updated successfully";

        if (command.newStatus() == RideStatus.COMPLETED) {
            // Validate required fields for completion
            if (command.distanceKm() == null) {
                throw new IllegalArgumentException("Distance is required when completing a ride");
            }
            if (command.durationMin() == null) {
                throw new IllegalArgumentException("Duration is required when completing a ride");
            }

            ride.setCompletedAt(LocalDateTime.now());
            ride.setDistanceKm(command.distanceKm());
            ride.setDurationMin(command.durationMin());
            ride.setDemandMultiplier(command.demandMultiplier() != null ? command.demandMultiplier() : 1.0);

            // Calculate fare using the fare calculation handler
            CalculateFareQuery fareQuery = new CalculateFareQuery(
                    ride.getDistanceKm(),
                    ride.getDurationMin(),
                    ride.getRideType(),
                    ride.getDemandMultiplier()
            );
            FareCalculationResult fareResult = calculateFareQueryHandler.handle(fareQuery);

            log.info("Fare calculated for ride: rideId={}, amount={}", command.rideId(), fareResult.totalFare());

            // Create pending payment with calculated fare (fare is only stored in payment, not ride)
            PaymentEntity payment = PaymentEntity.builder()
                    .ride(ride)
                    .amount(fareResult.totalFare())
                    .method(com.uber.backend.payment.domain.enums.PaymentMethod.CREDIT_CARD)  // Default, will be updated
                    .status(PaymentStatus.PENDING)
                    .build();

            payment = paymentRepository.save(payment);
            message = "Ride completed successfully. Pending payment created with fare: " + fareResult.totalFare();
            log.info("Ride completed and pending payment created: rideId={}, paymentId={}, fare={}",
                    command.rideId(), payment.getId(), fareResult.totalFare());
        }

        // Update ride status
        ride.setStatus(command.newStatus());
        ride = rideRepository.save(ride);

        return new UpdateRideStatusResult(
                ride.getId(),
                oldStatus,
                command.newStatus(),
                ride.getDriver() != null ? ride.getDriver().getId() : null,
                message
        );
    }

    private void validateStatusTransition(RideStatus oldStatus, RideStatus newStatus) {
        // Valid transitions:
        // REQUESTED -> ACCEPTED or CANCELLED
        // ACCEPTED -> IN_PROGRESS or CANCELLED
        // IN_PROGRESS -> COMPLETED or CANCELLED

        if (oldStatus == RideStatus.COMPLETED || oldStatus == RideStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update status of a ride that is already " + oldStatus);
        }

        if (oldStatus == RideStatus.REQUESTED && newStatus != RideStatus.ACCEPTED && newStatus != RideStatus.CANCELLED) {
            throw new IllegalStateException("REQUESTED ride can only transition to ACCEPTED or CANCELLED");
        }

        if (oldStatus == RideStatus.ACCEPTED && newStatus != RideStatus.IN_PROGRESS && newStatus != RideStatus.CANCELLED) {
            throw new IllegalStateException("ACCEPTED ride can only transition to IN_PROGRESS or CANCELLED");
        }

        if (oldStatus == RideStatus.IN_PROGRESS && newStatus != RideStatus.COMPLETED && newStatus != RideStatus.CANCELLED) {
            throw new IllegalStateException("IN_PROGRESS ride can only transition to COMPLETED or CANCELLED");
        }
    }
}
