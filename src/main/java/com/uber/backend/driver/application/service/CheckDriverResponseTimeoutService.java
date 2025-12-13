package com.uber.backend.driver.application.service;

import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.ride.infrastructure.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service that checks for driver invitation timeouts.
 * If a driver doesn't respond within 60 seconds, the ride is automatically
 * set to DENIED status and will be picked up by the poller for re-assignment.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CheckDriverResponseTimeoutService {

    private final RideRepository rideRepository;
    private static final int TIMEOUT_SECONDS = 60;

    /**
     * Runs every 10 seconds to check for timed-out driver invitations.
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void checkForTimedOutInvitations() {
        // Calculate cutoff time (60 seconds ago)
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(TIMEOUT_SECONDS);

        // Find all INVITED rides where invitedAt is older than 60 seconds
        List<RideEntity> timedOutRides = rideRepository.findTimedOutInvitations(
                RideStatus.INVITED,
                cutoffTime
        );

        if (timedOutRides.isEmpty()) {
            return;
        }

        for (RideEntity ride : timedOutRides) {
            // Add current driver to denied list
            if (ride.getDriver() != null && !ride.getDeniedDriverIds().contains(ride.getDriver().getId())) {
                ride.getDeniedDriverIds().add(ride.getDriver().getId());
                log.info("Added driver {} to denied list for ride {}", ride.getDriver().getId(), ride.getId());
            }

            // Change status to DENIED so poller can pick it up
            ride.setStatus(RideStatus.DENIED);

            // Clear driver assignment
            ride.setDriver(null);

            // Clear invitation timestamp
            ride.setInvitedAt(null);

            rideRepository.save(ride);
        }
    }
}
