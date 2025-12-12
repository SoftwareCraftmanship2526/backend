package com.uber.backend.ride.application.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompleteRideCommand(
    @NotNull(message = "Ride ID is required")
    Long rideId,

    @NotNull(message = "Distance is required")
    @Positive(message = "Distance must be positive")
    Double distanceKm,

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    Integer durationMin,

    Double demandMultiplier  // Optional, defaults to 1.0
) {}
