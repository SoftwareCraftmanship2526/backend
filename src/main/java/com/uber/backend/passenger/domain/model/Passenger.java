package com.uber.backend.passenger.domain.model;

import com.uber.backend.driver.domain.model.Account;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

    @DecimalMin(value = "0.0", message = "Passenger rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Passenger rating must be at most 5.0")
    private Double passengerRating;

    @Builder.Default
    private List<String> savedAddresses = new ArrayList<>();

    @Builder.Default
    private List<Long> rideIds = new ArrayList<>();

    public boolean hasSavedAddresses() {
        return savedAddresses != null && !savedAddresses.isEmpty();
    }
}
