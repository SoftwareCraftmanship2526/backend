package com.uber.backend.driver.application.dto;

import com.uber.backend.ride.domain.enums.RideType;

/**
 * Response DTO for vehicle information.
 * Used to return vehicle data from the application layer to the API layer,
 * avoiding circular references and N+1 query issues from entity relationships.
 */
public record VehicleDTO(
        Long id,
        String licensePlate,
        String model,
        String color,
        RideType type,
        Long driverId
) {}
