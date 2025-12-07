package com.uber.backend.driver.application.dto;

import com.uber.backend.ride.domain.enums.RideType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for updating vehicle information.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVehicleRequest {
    
    private String model;
    private String color;
    private RideType type;
}
