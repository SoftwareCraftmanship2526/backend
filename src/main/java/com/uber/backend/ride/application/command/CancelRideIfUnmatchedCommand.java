package com.uber.backend.ride.application.command;

public record CancelRideIfUnmatchedCommand (Long rideId) {}
