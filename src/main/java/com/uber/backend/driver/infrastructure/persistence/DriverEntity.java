package com.uber.backend.driver.infrastructure.persistence;

import com.uber.backend.ride.infrastructure.persistence.RideEntity;
import com.uber.backend.shared.domain.valueobject.Location;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverEntity extends AccountEntity {

    @Column(name = "driver_rating")
    private Double driverRating;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "current_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "current_longitude"))
    })
    private Location currentLocation;

    @OneToOne(mappedBy = "driver", cascade = CascadeType.ALL)
    private VehicleEntity currentVehicle;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RideEntity> rides = new ArrayList<>();
}
