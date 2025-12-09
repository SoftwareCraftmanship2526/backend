package com.uber.backend.driver.application.command;

public record SetCurrentVehicleCommand(
        Long vehicleId,
        Long driverId
) {}
