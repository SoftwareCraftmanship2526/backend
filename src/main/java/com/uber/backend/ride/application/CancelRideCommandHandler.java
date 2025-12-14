package com.uber.backend.ride.application;

import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.payment.domain.enums.PaymentMethod;
import com.uber.backend.payment.domain.enums.PaymentStatus;
import com.uber.backend.payment.infrastructure.persistence.PaymentEntity;
import com.uber.backend.ride.application.command.CancelRideCommand;
import com.uber.backend.ride.application.command.CancelRideResult;
import com.uber.backend.ride.application.exception.DriverNotFoundException;
import com.uber.backend.ride.application.exception.RideNotFoundException;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelRideCommandHandler {
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;

    private static final BigDecimal BASE_CANCELLATION_FEE = new BigDecimal("5.00");
    private static final BigDecimal ADDITIONAL_FEE_PER_MINUTE = new BigDecimal("1.00");
    private static final int CANCELLATION_FREE_PERIOD_MINUTES = 5;

    @Transactional
    public CancelRideResult handle(CancelRideCommand command, Long passengerId) {
        RideEntity rideEntity = rideRepository.findById(command.rideId()).orElse(null);
        if (rideEntity == null) {
            throw new RideNotFoundException(command.rideId());
        }

        // Verify passenger authorization
        if (!rideEntity.getPassenger().getId().equals(passengerId)) {
            throw new IllegalArgumentException("Unauthorized: This ride belongs to a different passenger");
        }

        // Check if ride is already cancelled
        if (rideEntity.getStatus() == RideStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot cancel ride " + command.rideId() + ": This ride has already been cancelled");
        }

        // Check if ride can be cancelled (only REQUESTED, INVITED, or ACCEPTED status allowed)
        if (rideEntity.getStatus() != RideStatus.REQUESTED && 
            rideEntity.getStatus() != RideStatus.INVITED && 
            rideEntity.getStatus() != RideStatus.ACCEPTED) {
            throw new IllegalArgumentException("Cannot cancel ride " + command.rideId() + ": Ride is already " + rideEntity.getStatus() + ". Only rides with REQUESTED, INVITED, or ACCEPTED status can be cancelled");
        }

        PaymentEntity paymentEntity = null;
        String message;

        // Check if ride was accepted by driver
        if (rideEntity.getStatus() == RideStatus.ACCEPTED && rideEntity.getAcceptedAt() != null) {
            // Calculate time since acceptance
            Duration timeSinceAcceptance = Duration.between(rideEntity.getAcceptedAt(), LocalDateTime.now());
            long minutesSinceAcceptance = timeSinceAcceptance.toMinutes();

            if (minutesSinceAcceptance < CANCELLATION_FREE_PERIOD_MINUTES) {
                // Less than 5 minutes - no charge
                message = "Ride cancelled. No cancellation fee (cancelled within " + minutesSinceAcceptance + " minutes)";
            } else {
                // 5 minutes or more - calculate progressive fee
                // Base fee €5.00 at 5 minutes, then +€1.00 for each additional minute
                long additionalMinutes = minutesSinceAcceptance - CANCELLATION_FREE_PERIOD_MINUTES;
                BigDecimal cancellationFee = BASE_CANCELLATION_FEE.add(
                        ADDITIONAL_FEE_PER_MINUTE.multiply(BigDecimal.valueOf(additionalMinutes))
                );

                log.info("Ride {} cancelled {} minutes after acceptance - charging €{} cancellation fee (€5 base + €{} for {} extra minutes)",
                        command.rideId(), minutesSinceAcceptance, cancellationFee,
                        ADDITIONAL_FEE_PER_MINUTE.multiply(BigDecimal.valueOf(additionalMinutes)), additionalMinutes);
                message = "Ride cancelled. Cancellation fee of €" + cancellationFee + " is pending payment (cancelled after " + minutesSinceAcceptance + " minutes)";

                // Create payment entity
                paymentEntity = PaymentEntity.builder()
                        .amount(cancellationFee)
                        .method(PaymentMethod.CREDIT_CARD)
                        .status(PaymentStatus.PENDING)
                        .ride(rideEntity)
                        .build();
                
                rideEntity.setPayment(paymentEntity);
            }
        } else {
            // Ride was cancelled before driver accepted (REQUESTED or INVITED status) - no charge
            log.info("Ride {} cancelled before driver acceptance - no charge", command.rideId());
            message = "Ride cancelled. No cancellation fee (cancelled before driver acceptance)";
        }

        // Update ride status
        rideEntity.setStatus(RideStatus.CANCELLED);
        rideEntity.setCancelledAt(LocalDateTime.now());

        rideRepository.save(rideEntity);

        // Set driver as available if assigned
        if (rideEntity.getDriver() != null) {
            DriverEntity driverEntity = driverRepository.findById(rideEntity.getDriver().getId())
                    .orElseThrow(() -> new DriverNotFoundException(rideEntity.getDriver().getId()));
            driverEntity.setIsAvailable(true);
            driverRepository.save(driverEntity);
        }

        // Build payment info if there was a cancellation fee
        CancelRideResult.PaymentInfo paymentInfo = null;
        if (paymentEntity != null) {
            paymentInfo = new CancelRideResult.PaymentInfo(
                    paymentEntity.getId(),
                    paymentEntity.getAmount(),
                    paymentEntity.getMethod(),
                    paymentEntity.getStatus()
            );
        }

        return new CancelRideResult(message, paymentInfo);
    }
}
