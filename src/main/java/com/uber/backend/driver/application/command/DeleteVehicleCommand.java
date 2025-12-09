package com.uber.backend.driver.application.command;

public record DeleteVehicleCommand(
        Long vehicleId,
        Long driverId
) {}
