package com.uber.backend.driver.application.command;

import com.uber.backend.ride.domain.enums.RideType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateVehicleCommand(
        Long vehicleId,
        Long driverId,

        @NotBlank(message = "Model is required")
        String model,

        @NotBlank(message = "Color is required")
        String color,

        @NotNull(message = "Vehicle type is required")
        RideType type
) {}
