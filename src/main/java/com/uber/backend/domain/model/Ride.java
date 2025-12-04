package com.uber.backend.domain.model;

import com.uber.backend.domain.embeddable.Location;
import com.uber.backend.domain.enums.RideStatus;
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
    private RideStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private BigDecimal fareAmount;
    private Location pickupLocation;
    private Location dropoffLocation;

    // References by ID instead of entities
    private Long passengerId;
    private Long driverId;
    private Long vehicleId;
    private Long paymentId;

    @Builder.Default
    private List<Long> ratingIds = new ArrayList<>();

    public void initializeRide() {
        if (this.requestedAt == null) {
            this.requestedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = RideStatus.REQUESTED;
        }
    }

    public void startRide() {
        if (this.status == RideStatus.REQUESTED) {
            this.status = RideStatus.IN_PROGRESS;
            this.startedAt = LocalDateTime.now();
        }
    }

    public void completeRide() {
        if (this.status == RideStatus.IN_PROGRESS) {
            this.status = RideStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }
    }

    public void cancelRide() {
        if (this.status == RideStatus.REQUESTED || this.status == RideStatus.IN_PROGRESS) {
            this.status = RideStatus.CANCELLED;
        }
    }

    public boolean isActive() {
        return this.status == RideStatus.REQUESTED || this.status == RideStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return this.status == RideStatus.COMPLETED;
    }

    public Long getDurationInMinutes() {
        if (startedAt != null && completedAt != null) {
            return java.time.Duration.between(startedAt, completedAt).toMinutes();
        }
        return null;
    }

    public void setFare(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.fareAmount = amount;
        }
    }
}
