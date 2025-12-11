package com.uber.backend.ride.application.command;

import com.uber.backend.ride.domain.enums.RideStatus;

public record CreateRideResult(
        Long rideId,
        Long passengerId,
        Long driverId,
        String pickupAddress,
        String dropoffAddress,
        RideStatus status,
        String message
) {}
