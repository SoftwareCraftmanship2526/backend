package com.uber.backend.ride.api.dto;

import com.uber.backend.shared.domain.valueobject.Location;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Data
public class RideRequestDto {
    private String passengerId;
    private String type;
    private Location start;
    private Location end;
    private int durationMin;
    private Double demandMultiplier;
}