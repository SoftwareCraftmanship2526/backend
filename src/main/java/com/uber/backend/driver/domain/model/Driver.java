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
    private Long currentVehicleId;  // Reference by ID

    @Builder.Default
    private List<Long> rideIds = new ArrayList<>();  // Reference by ID

    public boolean canAcceptRides() {
        return isAvailable != null && isAvailable && hasGoodRating();
    }

    public boolean hasGoodRating() {
        return driverRating != null && driverRating >= 4.0;
    }

    public void updateRating(double newRating) {
        if (newRating >= 0 && newRating <= 5.0) {
            this.driverRating = newRating;
        }
    }

    public void setAvailability(boolean available) {
        this.isAvailable = available;
    }

    public void updateLocation(Location newLocation) {
        this.currentLocation = newLocation;
    }

    public boolean hasValidLicense() {
        return licenseNumber != null && !licenseNumber.isBlank();
    }
}
