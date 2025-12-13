package com.uber.backend.ride.application.command;

import com.uber.backend.payment.domain.enums.PaymentMethod;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.domain.enums.RideType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateRideCommand(
        @NotNull(message = "Passenger ID is required")
        Long passengerId,

        Long driverId,

        @NotNull(message = "Pickup address is required")
        String pickupAddress,

        @NotNull(message = "Dropoff address is required")
        String dropoffAddress,

        Double pickupLatitude,
        Double pickupLongitude,
        Double dropoffLatitude,
        Double dropoffLongitude,

        @NotNull(message = "Ride type is required")
        RideType rideType,

        @NotNull(message = "Ride status is required")
        RideStatus status
) {}
