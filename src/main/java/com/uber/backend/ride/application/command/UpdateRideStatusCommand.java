package com.uber.backend.ride.application.command;

import com.uber.backend.ride.domain.enums.RideStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRideStatusCommand(
        @NotNull(message = "Ride ID is required")
        Long rideId,

        @NotNull(message = "New status is required")
        RideStatus newStatus,

        Long driverId,  // Required when accepting ride

        Double distanceKm,  // Required when completing ride
        Integer durationMin,  // Required when completing ride
        Double demandMultiplier  // Optional, defaults to 1.0
) {}
