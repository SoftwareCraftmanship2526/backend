package com.uber.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "passengers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Passenger extends Account {

    @Column(name = "passenger_rating")
    private Double passengerRating;

    @ElementCollection
    @CollectionTable(name = "passenger_saved_addresses", joinColumns = @JoinColumn(name = "passenger_id"))
    @Column(name = "address")
    @Builder.Default
    private List<String> savedAddresses = new ArrayList<>();

    @OneToMany(mappedBy = "passenger", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Ride> rides = new ArrayList<>();
}
