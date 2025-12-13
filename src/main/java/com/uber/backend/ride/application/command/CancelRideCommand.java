package com.uber.backend.ride.application.command;


public record CancelRideCommand (
        Long rideId
) {}
