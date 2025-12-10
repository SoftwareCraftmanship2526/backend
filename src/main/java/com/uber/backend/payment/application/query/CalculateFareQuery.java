package com.uber.backend.payment.application.query;

import com.uber.backend.ride.domain.enums.RideType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CalculateFareQuery(
        @NotNull(message = "Distance is required")
        @Min(value = 0, message = "Distance must be positive")
        Double distanceKm,

        @NotNull(message = "Duration is required")
        @Min(value = 0, message = "Duration must be positive")
        Integer durationMin,

        @NotNull(message = "Ride type is required")
        RideType rideType,

        @NotNull(message = "Demand multiplier is required")
        @Min(value = 1, message = "Demand multiplier must be at least 1.0")
        Double demandMultiplier
) {}
