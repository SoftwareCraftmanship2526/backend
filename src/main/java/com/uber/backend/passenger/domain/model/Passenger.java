package com.uber.backend.passenger.domain.model;

import com.uber.backend.driver.domain.model.Account;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Passenger extends Account {

    private Double passengerRating;

    @Builder.Default
    private List<String> savedAddresses = new ArrayList<>();

    @Builder.Default
    private List<Long> rideIds = new ArrayList<>();  // Reference by ID instead of entity

    public void addSavedAddress(String address) {
        if (address != null && !address.isBlank() && !savedAddresses.contains(address)) {
            savedAddresses.add(address);
        }
    }

    public void removeSavedAddress(String address) {
        savedAddresses.remove(address);
    }

    public boolean hasGoodRating() {
        return passengerRating != null && passengerRating >= 4.0;
    }

    public void updateRating(double newRating) {
        if (newRating >= 0 && newRating <= 5.0) {
            this.passengerRating = newRating;
        }
    }
}
