package com.uber.backend.ride.application.command;

public record DriverAssignCommand (
    Long rideId,
    Long driverId
) {}
