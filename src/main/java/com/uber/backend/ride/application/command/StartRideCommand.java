package com.uber.backend.ride.application.command;

public record StartRideCommand(
    Long rideId
) {}
