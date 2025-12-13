package com.uber.backend.ride.application.query;

import jakarta.validation.constraints.NotNull;

public record GetRideQuery(
        @NotNull(message = "Ride ID is required")
        Long rideId
) {}
