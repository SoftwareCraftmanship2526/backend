package com.uber.backend.ride.application.command;

import com.uber.backend.shared.domain.valueobject.Location;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Data
public class RequestRideCommand {
    private String passengerId;
    private String type;
    private Location start;
    private Location end;
    private int durationMin;
    private Double demandMultiplier;
}