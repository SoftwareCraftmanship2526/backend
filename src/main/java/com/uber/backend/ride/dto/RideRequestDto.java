package com.uber.backend.ride.dto;

import com.uber.backend.ride.domain.enums.RideStatus;
import com.uber.backend.shared.domain.valueobject.Location;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestDto {
    private RideStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private BigDecimal fareAmount;
    private Location pickupLocation;
    private Location dropOffLocation;
    private Long passengerId;
    private Long driverId;
    private Long vehicleId;
    private Long paymentId;
    private List<Long> ratingIds;

}
