package com.uber.backend.driver.domain.model;

import com.uber.backend.shared.domain.valueobject.Location;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Driver extends Account {

    @DecimalMin(value = "0.0", message = "Driver rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Driver rating must be at most 5.0")
    private Double driverRating;

    @NotNull(message = "Availability status is required")
    private Boolean isAvailable;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @Valid
    private Location currentLocation;

    private Long currentVehicleId;

    @Builder.Default
    private List<Long> rideIds = new ArrayList<>();
}
