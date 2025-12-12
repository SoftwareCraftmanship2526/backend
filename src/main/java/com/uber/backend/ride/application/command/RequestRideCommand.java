package com.uber.backend.ride.application.command;

import com.uber.backend.ride.domain.enums.RideType;

import jakarta.validation.constraints.NotNull;

public record RequestRideCommand(
        @NotNull(message = "Pickup address is required")
        String pickupAddress,
        
        @NotNull(message = "Pickup latitude is required")
        Double pickupLatitude,
        
        @NotNull(message = "Pickup longitude is required")
        Double pickupLongitude,
        
        @NotNull(message = "Dropoff address is required")
        String dropoffAddress,
        
        @NotNull(message = "Dropoff latitude is required")
        Double dropoffLatitude,
        
        @NotNull(message = "Dropoff longitude is required")
        Double dropoffLongitude,
        
        @NotNull(message = "Ride type is required")
        RideType rideType
) {}