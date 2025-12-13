package com.uber.backend.driver.application.command;

import jakarta.validation.constraints.NotNull;

public record UpdateLocationCommand(
        @NotNull(message = "Latitude is required")
        Double latitude,
        
        @NotNull(message = "Longitude is required")
        Double longitude
) {}
