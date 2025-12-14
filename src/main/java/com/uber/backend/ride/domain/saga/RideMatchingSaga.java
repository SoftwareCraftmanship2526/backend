package com.uber.backend.ride.domain.saga;

import com.uber.backend.ride.application.command.CancelRideIfUnmatchedCommand;
import com.uber.backend.ride.application.command.RideRequestResult;
import com.uber.backend.ride.infrastructure.DelayedCommandScheduler;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RideMatchingSaga {

    private final DelayedCommandScheduler scheduler;

    public RideMatchingSaga(DelayedCommandScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @EventListener
    public void on(RideRequestResult event) {
        scheduler.schedule(
                new CancelRideIfUnmatchedCommand(event.rideId()),
                event.requestedAt().plus(Duration.ofMinutes(5))
        );
    }
}

