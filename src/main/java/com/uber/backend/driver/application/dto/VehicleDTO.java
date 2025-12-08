package com.uber.backend.driver.application.dto;

import com.uber.backend.ride.domain.enums.RideType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for vehicle information.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {
    
    private Long id;
    private String licensePlate;
    private String model;
    private String color;
    private RideType type;
    private Long driverId;
}
