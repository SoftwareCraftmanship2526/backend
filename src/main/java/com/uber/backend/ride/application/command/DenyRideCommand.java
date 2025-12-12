package com.uber.backend.ride.application.command;

import jakarta.validation.constraints.NotNull;

public record DenyRideCommand(
    @NotNull(message = "Ride ID is required")
    Long rideId
) {}
