package com.uber.backend.ride.domain.model;

import com.uber.backend.ride.domain.event.RideCancelledEvent;
import com.uber.backend.shared.domain.DomainEvents;
import com.uber.backend.shared.domain.valueobject.Location;
import com.uber.backend.ride.domain.enums.RideStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ride {

    private Long id;

    @NotNull(message = "Ride status is required")
    private RideStatus status;

    @NotNull(message = "Requested time is required")
    private LocalDateTime requestedAt;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Positive(message = "Fare amount must be positive")
    private BigDecimal fareAmount;

    @NotNull(message = "Pickup location is required")
    @Valid
    private Location pickupLocation;

    @NotNull(message = "Dropoff location is required")
    @Valid
    private Location dropoffLocation;

    @NotNull(message = "Passenger ID is required")
    private Long passengerId;

    private Long driverId;
    private Long vehicleId;
    private Long paymentId;

    @Builder.Default
    private List<Long> ratingIds = new ArrayList<>();

    public void cancelIfUnmatched() {
        if (status == RideStatus.REQUESTED) {
            this.status = RideStatus.CANCELLED;
            DomainEvents.raise(new RideCancelledEvent(id));
        }
    }
}
