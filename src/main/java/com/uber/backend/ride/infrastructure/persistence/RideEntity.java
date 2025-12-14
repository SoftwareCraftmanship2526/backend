package com.uber.backend.ride.infrastructure.persistence;

import com.uber.backend.passenger.infrastructure.persistence.PassengerEntity;
import com.uber.backend.driver.infrastructure.persistence.DriverEntity;
import com.uber.backend.driver.infrastructure.persistence.VehicleEntity;
import com.uber.backend.payment.infrastructure.persistence.PaymentEntity;
import com.uber.backend.shared.domain.valueobject.Location;
import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.ride.domain.enums.RideType;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Entity
@Table(name = "rides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "ride_type", nullable = false)
    private RideType rideType;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "duration_min")
    private Integer durationMin;

    @Column(name = "demand_multiplier")
    private Double demandMultiplier;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "pickup_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "pickup_longitude")),
            @AttributeOverride(name = "address", column = @Column(name = "pickup_address"))
    })
    private Location pickupLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "dropoff_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "dropoff_longitude")),
            @AttributeOverride(name = "address", column = @Column(name = "dropoff_address"))
    })
    private Location dropoffLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private PassengerEntity passenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private DriverEntity driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private VehicleEntity vehicle;

    @OneToOne(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
    private PaymentEntity payment;

    @ElementCollection
    @CollectionTable(name = "ride_denied_drivers", joinColumns = @JoinColumn(name = "ride_id"))
    @Column(name = "driver_id")
    @Builder.Default
    private List<Long> deniedDriverIds = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.requestedAt == null) {
            this.requestedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = RideStatus.REQUESTED;
        }
    }

    public void cancelIfUnmatched() {
        if (this.status == RideStatus.REQUESTED) {
            this.status = RideStatus.CANCELLED;
            this.cancelledAt = LocalDateTime.now();
        }
    }
}
