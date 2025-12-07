package com.uber.backend.driver.application.dto;

import com.uber.backend.ride.domain.enums.RideType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for adding a new vehicle.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddVehicleRequest {
    
    @NotBlank(message = "License plate is required")
    @Pattern(regexp = "^[A-Z0-9-]{3,15}$", message = "License plate must be 3-15 characters (uppercase letters, numbers, hyphens)")
    private String licensePlate;
    
    @NotBlank(message = "Model is required")
    private String model;
    
    @NotBlank(message = "Color is required")
    private String color;
    
    @NotNull(message = "Vehicle type is required")
    private RideType type;
}
