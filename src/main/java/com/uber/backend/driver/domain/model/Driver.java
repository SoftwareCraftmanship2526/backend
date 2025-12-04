package com.uber.backend.driver.domain.model;

import com.uber.backend.shared.domain.valueobject.Location;
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

    private Double driverRating;
    private Boolean isAvailable;
    private String licenseNumber;
    private Location currentLocation;
    private Long currentVehicleId;

    @Builder.Default
    private List<Long> rideIds = new ArrayList<>();
}
