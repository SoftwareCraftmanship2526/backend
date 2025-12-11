package com.uber.backend.ride.application;

import com.uber.backend.payment.infrastructure.persistence.PaymentEntity;
import com.uber.backend.ride.application.query.GetRideQuery;
import com.uber.backend.ride.application.query.RideResult;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Query handler for retrieving a ride with its payment information.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GetRideQueryHandler {

    private final RideRepository rideRepository;

    @Transactional(readOnly = true)
    public RideResult handle(GetRideQuery query) {
        log.info("Getting ride: rideId={}", query.rideId());

        // Get ride
        RideEntity ride = rideRepository.findById(query.rideId())
                .orElseThrow(() -> new IllegalArgumentException("Ride not found: " + query.rideId()));

        // Build payment info if payment exists
        RideResult.PaymentInfo paymentInfo = null;
        PaymentEntity payment = ride.getPayment();
        if (payment != null) {
            paymentInfo = new RideResult.PaymentInfo(
                    payment.getId(),
                    payment.getAmount(),
                    payment.getMethod(),
                    payment.getStatus(),
                    payment.getTransactionId()
            );
        }

        // Build and return result
        return new RideResult(
                ride.getId(),
                ride.getPassenger().getId(),
                ride.getDriver() != null ? ride.getDriver().getId() : null,
                ride.getPickupLocation().address(),
                ride.getDropoffLocation().address(),
                ride.getRideType(),
                ride.getStatus(),
                ride.getRequestedAt(),
                ride.getStartedAt(),
                ride.getCompletedAt(),
                ride.getDistanceKm(),
                ride.getDurationMin(),
                ride.getDemandMultiplier(),
                paymentInfo
        );
    }
}
