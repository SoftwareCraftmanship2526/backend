package com.uber.backend.ride.application.command;

public record CompleteRideCommand (
    Long rideId
) {}
