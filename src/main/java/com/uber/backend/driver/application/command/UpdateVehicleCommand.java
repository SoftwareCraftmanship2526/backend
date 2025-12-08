package com.uber.backend.driver.application.command;

import com.uber.backend.ride.domain.enums.RideType;

public record UpdateVehicleCommand(
        Long vehicleId,
        Long driverId,
        String model,
        String color,
        RideType type
) {}
