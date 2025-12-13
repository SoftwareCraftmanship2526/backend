package com.uber.backend.ride.application.command;

import com.uber.backend.ride.domain.enums.RideStatus;

import java.time.LocalDateTime;

public record RideRequestResult (
        Long rideId,
        Long passengerId,
        RideStatus status,
        LocalDateTime requestedAt
) {}
