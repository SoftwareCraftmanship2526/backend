package com.uber.backend.domain.entity;

import com.uber.backend.domain.embeddable.Location;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Driver extends Account {

    @Column(name = "driver_rating")
    private Double driverRating;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @Column(name = "license_number", unique = true, nullable = false)
    private String licenseNumber;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "latitude", column = @Column(name = "current_latitude")),
        @AttributeOverride(name = "longitude", column = @Column(name = "current_longitude")),
        @AttributeOverride(name = "address", column = @Column(name = "current_address"))
    })
    private Location currentLocation;

    @OneToOne(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    private Vehicle currentVehicle;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Ride> rides = new ArrayList<>();
}
