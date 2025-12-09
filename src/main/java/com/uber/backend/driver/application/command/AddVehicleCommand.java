package com.uber.backend.driver.application.command;

import com.uber.backend.ride.domain.enums.RideType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AddVehicleCommand(
        Long driverId,

        @NotBlank(message = "License plate is required")
        @Pattern(regexp = "^[A-Z0-9-]{3,15}$", message = "License plate must be 3-15 characters (uppercase letters, numbers, hyphens)")
        String licensePlate,

        @NotBlank(message = "Model is required")
        String model,

        @NotBlank(message = "Color is required")
        String color,

        @NotNull(message = "Vehicle type is required")
        RideType type
) {}
