package com.uber.backend.ride.application.command;

import com.uber.backend.ride.domain.enums.RideStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Data
public class RideResponseCommand {
    private Long rideId;
    private Long passengerId;
    private RideStatus status;
    private LocalDateTime requestedAt;
    private BigDecimal price;
}
