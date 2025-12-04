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
    private List<Long> rideIds = new ArrayList<>();
}
