package com.uber.backend.ride.application.query;

import com.uber.backend.shared.domain.valueobject.Location;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PriceRequestQuery {
    private Location start;
    private Location end;
    private int durationMin;
    private double demandMultiplier;
}
