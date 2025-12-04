package com.uber.backend.passenger.infrastructure.persistence;

import com.uber.backend.driver.infrastructure.persistence.AccountEntity;
import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "passengers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerEntity extends AccountEntity {

    @Column(name = "passenger_rating")
    private Double passengerRating;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "passenger_saved_addresses", joinColumns = @JoinColumn(name = "passenger_id"))
    @Column(name = "address")
    @Builder.Default
    private List<String> savedAddresses = new ArrayList<>();

    @OneToMany(mappedBy = "passenger", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RideEntity> rides = new ArrayList<>();
}
