package com.uber.backend.ride.application;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.payment.application.CalculateFareQueryHandler;
import com.uber.backend.payment.application.query.CalculateFareQuery;
import com.uber.backend.payment.application.query.FareCalculationResult;
import com.uber.backend.payment.domain.enums.PaymentMethod;
import com.uber.backend.payment.domain.enums.PaymentStatus;
import com.uber.backend.payment.infrastructure.persistence.PaymentEntity;
import com.uber.backend.payment.infrastructure.repository.PaymentRepository;
import com.uber.backend.ride.application.command.CompleteRideCommand;
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
public class CompleteRideCommandHandler {
    private final RideRepository rideRepository;
    private final PaymentRepository paymentRepository;
    private final CalculateFareQueryHandler calculateFareQueryHandler;
    private final DriverRepository driverRepository;

    @Transactional
    public RideResult handle(CompleteRideCommand command, Long driverId) {
        // Find the ride
        RideEntity rideEntity = rideRepository.findById(command.rideId())
                .orElseThrow(() -> new RideNotFoundException(command.rideId()));

        // Validate current status
        if (rideEntity.getStatus() != RideStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Ride must be in IN_PROGRESS status to be completed. Current status: " + rideEntity.getStatus());
        }

        // Validate that the logged-in driver is the one assigned to this ride
        if (rideEntity.getDriver() == null || !rideEntity.getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("Cannot complete ride " + command.rideId() + ": This ride is assigned to a different driver");
        }

        // Set completion time and ride details
        rideEntity.setCompletedAt(LocalDateTime.now());
        rideEntity.setDistanceKm(command.distanceKm());
        rideEntity.setDurationMin(command.durationMin());
        rideEntity.setDemandMultiplier(command.demandMultiplier() != null ? command.demandMultiplier() : 1.0);

        // Calculate fare using the fare calculation handler
        CalculateFareQuery fareQuery = new CalculateFareQuery(
                rideEntity.getDistanceKm(),
                rideEntity.getDurationMin(),
                rideEntity.getRideType(),
                rideEntity.getDemandMultiplier()
        );
        FareCalculationResult fareResult = calculateFareQueryHandler.handle(fareQuery);

        // Create pending payment with calculated fare
        PaymentEntity payment = PaymentEntity.builder()
                .ride(rideEntity)
                .amount(fareResult.totalFare())
                .method(PaymentMethod.CREDIT_CARD)  // Default, will be updated when processed
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);
        
        // Set bidirectional relationship
        rideEntity.setPayment(payment);

        // Update ride status
        rideEntity.setStatus(RideStatus.COMPLETED);
        rideEntity = rideRepository.save(rideEntity);
        DriverEntity driver = rideEntity.getDriver();
        driver.setIsAvailable(true);
        driverRepository.save(driver);
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
