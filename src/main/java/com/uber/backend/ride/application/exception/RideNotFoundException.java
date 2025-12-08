package com.uber.backend.ride.application.exception;

public class RideNotFoundException extends RuntimeException {

    public RideNotFoundException(Long rideId) {
        super("Ride with ID " + rideId + " was not found.");
    }

    public RideNotFoundException(String message) {
        super(message);
    }
}
