package com.uber.backend.domain.model;

import com.uber.backend.domain.enums.RideType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    private Long id;
    private String licensePlate;
    private String model;
    private String color;
    private RideType type;
    private Long driverId;  // Reference by ID

    @Builder.Default
    private List<Long> rideIds = new ArrayList<>();  // Reference by ID

    public boolean hasValidLicensePlate() {
        return licensePlate != null && !licensePlate.isBlank();
    }

    public boolean isValid() {
        return hasValidLicensePlate()
                && model != null && !model.isBlank()
                && color != null && !color.isBlank()
                && type != null;
    }

    public boolean hasDriver() {
        return driverId != null;
    }

    public String getDescription() {
        return color + " " + model + " (" + licensePlate + ")";
    }
}
