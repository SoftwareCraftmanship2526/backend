package com.uber.backend.ride.application.command;

public record DriverAcceptCommand (
    Long rideId
) {}
