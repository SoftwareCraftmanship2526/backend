package com.uber.backend.driver.application.command;

import com.uber.backend.ride.domain.enums.RideType;

public record AddVehicleCommand(
        Long driverId,
        String licensePlate,
        String model,
        String color,
        RideType type
) {}
