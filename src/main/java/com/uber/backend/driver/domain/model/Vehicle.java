package com.uber.backend.driver.domain.model;

import com.uber.backend.ride.domain.enums.RideType;
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
    private Long driverId;

    @Builder.Default
    private List<Long> rideIds = new ArrayList<>();
}
