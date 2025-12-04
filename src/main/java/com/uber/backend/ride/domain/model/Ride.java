package com.uber.backend.ride.domain.model;

import com.uber.backend.shared.domain.valueobject.Location;
import com.uber.backend.ride.domain.enums.RideStatus;
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

    private Long passengerId;
    private Long driverId;
    private Long vehicleId;
    private Long paymentId;

    @Builder.Default
    private List<Long> ratingIds = new ArrayList<>();
}
