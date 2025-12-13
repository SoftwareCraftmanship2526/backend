package com.uber.backend.ride.application.command;

import com.uber.backend.ride.domain.enums.RideStatus;

public record UpdateRideStatusResult(
        Long rideId,
        RideStatus oldStatus,
        RideStatus newStatus,
        Long driverId,
        String message
) {}
