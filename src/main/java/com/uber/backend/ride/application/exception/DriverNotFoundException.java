package com.uber.backend.ride.application.exception;

public class DriverNotFoundException extends RuntimeException {

    public DriverNotFoundException(Long rideId) {
        super("Driver with ID " + rideId + " was not found.");
    }

    public DriverNotFoundException(String message) {
        super(message);
    }
}
